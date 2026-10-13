package com.example.hexagonal.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.hexagonal.common.security.SecurityErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-for-jwt-token-provider-0123456789";

    private final JwtTokenProvider jwtTokenProvider =
            new JwtTokenProvider(new JwtProperties(SECRET, Duration.ofMinutes(30)));

    @Test
    void 발급한_토큰에서_subject와_권한을_복원한다() {
        String token = jwtTokenProvider.createAccessToken("member-uuid", List.of("ROLE_USER"));

        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getName()).isEqualTo("member-uuid");
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void 발급한_토큰마다_고유한_tokenId와_만료_시각을_가진다() {
        Instant before = Instant.now();
        JwtPrincipal first = principalOf(jwtTokenProvider.createAccessToken("member-uuid", List.of("ROLE_USER")));
        JwtPrincipal second = principalOf(jwtTokenProvider.createAccessToken("member-uuid", List.of("ROLE_USER")));

        assertThat(first.tokenId()).isNotBlank().isNotEqualTo(second.tokenId());
        assertThat(first.expiresAt())
                .isAfter(before.plus(Duration.ofMinutes(29)))
                .isBefore(before.plus(Duration.ofMinutes(31)));
    }

    @Test
    void tokenId가_없는_토큰은_INVALID_TOKEN으로_거부한다() {
        String token = Jwts.builder()
                .subject("member-uuid")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(token))
                .isInstanceOf(InvalidJwtException.class)
                .extracting("errorCode")
                .isEqualTo(SecurityErrorCode.INVALID_TOKEN);
    }

    private JwtPrincipal principalOf(String token) {
        return (JwtPrincipal) jwtTokenProvider.getAuthentication(token).getPrincipal();
    }

    @Test
    void 만료된_토큰은_EXPIRED_TOKEN으로_거부한다() {
        JwtTokenProvider expiredProvider =
                new JwtTokenProvider(new JwtProperties(SECRET, Duration.ofSeconds(-1)));
        String token = expiredProvider.createAccessToken("member-uuid", List.of("ROLE_USER"));

        assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(token))
                .isInstanceOf(InvalidJwtException.class)
                .extracting("errorCode")
                .isEqualTo(SecurityErrorCode.EXPIRED_TOKEN);
    }

    @Test
    void 다른_키로_서명된_토큰은_INVALID_TOKEN으로_거부한다() {
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                new JwtProperties("another-secret-key-for-jwt-token-provider-9876543210", Duration.ofMinutes(30)));
        String token = otherProvider.createAccessToken("member-uuid", List.of("ROLE_USER"));

        assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(token))
                .isInstanceOf(InvalidJwtException.class)
                .extracting("errorCode")
                .isEqualTo(SecurityErrorCode.INVALID_TOKEN);
    }

    @Test
    void 형식이_잘못된_토큰은_INVALID_TOKEN으로_거부한다() {
        assertThatThrownBy(() -> jwtTokenProvider.getAuthentication("not-a-jwt"))
                .isInstanceOf(InvalidJwtException.class)
                .extracting("errorCode")
                .isEqualTo(SecurityErrorCode.INVALID_TOKEN);
    }

    @Test
    void 서명_키가_256bit_미만이면_생성_시점에_거부한다() {
        assertThatThrownBy(() -> new JwtTokenProvider(new JwtProperties("short-key", Duration.ofMinutes(30))))
                .isInstanceOf(WeakKeyException.class);
    }
}
