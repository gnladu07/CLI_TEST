package com.example.hexagonal.common.security.jwt;

import java.security.Principal;
import java.time.Instant;

/**
 * 검증된 Access Token에서 복원한 인증 주체. 로그아웃 시 토큰 무효화에 필요한 tokenId와 만료 시각을 함께 보관한다.
 */
public record JwtPrincipal(
        String subject,
        String tokenId,
        Instant expiresAt
) implements Principal {

    @Override
    public String getName() {
        return subject;
    }
}
