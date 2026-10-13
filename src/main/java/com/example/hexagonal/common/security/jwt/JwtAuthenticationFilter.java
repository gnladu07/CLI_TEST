package com.example.hexagonal.common.security.jwt;

import com.example.hexagonal.common.security.SecurityErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Bean으로 등록하면 서블릿 필터로도 자동 등록되어 Security 체인 밖에서 한 번 더 실행되므로,
 * SecurityConfig에서 직접 생성해 Security 필터 체인에만 추가한다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRevocationChecker tokenRevocationChecker;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            try {
                Authentication authentication = authenticate(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug(
                        "JwtAuthenticationFilter : doFilterInternal : JWT 인증 성공 - subject={}, uri={}",
                        authentication.getName(),
                        request.getRequestURI()
                );
            } catch (InvalidJwtException e) {
                SecurityContextHolder.clearContext();
                request.setAttribute(SecurityErrorCode.REQUEST_ATTRIBUTE, e.getErrorCode());
                log.debug(
                        "JwtAuthenticationFilter : doFilterInternal : JWT 검증 실패 - code={}, uri={}",
                        e.getErrorCode(),
                        request.getRequestURI()
                );
            }
        }

        filterChain.doFilter(request, response);
    }

    private Authentication authenticate(String token) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        JwtPrincipal principal = (JwtPrincipal) authentication.getPrincipal();
        if (tokenRevocationChecker.isRevoked(principal.tokenId())) {
            throw new InvalidJwtException(SecurityErrorCode.REVOKED_TOKEN);
        }
        return authentication;
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            return StringUtils.hasText(token) ? token : null;
        }
        return null;
    }
}
