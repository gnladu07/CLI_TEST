package com.example.hexagonal.common.security.jwt;

import com.example.hexagonal.common.security.SecurityErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenProvider {

    private static final String ROLES_CLAIM = "roles";

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        // 256bit 미만의 키는 Keys.hmacShaKeyFor에서 WeakKeyException으로 기동 시점에 거부된다.
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = jwtProperties.accessTokenExpiration();
    }

    public String createAccessToken(String subject, Collection<String> roles) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + accessTokenExpiration.toMillis());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .claim(ROLES_CLAIM, List.copyOf(roles))
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        List<SimpleGrantedAuthority> authorities = extractRoles(claims).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        JwtPrincipal principal = new JwtPrincipal(
                claims.getSubject(),
                claims.getId(),
                claims.getExpiration().toInstant()
        );

        return UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
    }

    private Claims parseClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidJwtException(SecurityErrorCode.EXPIRED_TOKEN, e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtException(SecurityErrorCode.INVALID_TOKEN, e);
        }

        // 로그아웃 무효화는 jti와 만료 시각을 기준으로 하므로 둘 중 하나라도 없는 토큰은 받지 않는다.
        if (!StringUtils.hasText(claims.getId()) || claims.getExpiration() == null) {
            throw new InvalidJwtException(SecurityErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    private List<String> extractRoles(Claims claims) {
        List<?> roles = claims.get(ROLES_CLAIM, List.class);
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(String::valueOf)
                .toList();
    }
}
