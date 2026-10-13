package com.example.hexagonal.application.port.in;

import java.time.Instant;

/**
 * @param memberId  토큰의 subject(회원 ID)
 * @param tokenId   무효화할 Access Token의 jti
 * @param expiresAt 무효화할 Access Token의 만료 시각
 */
public record LogoutCommand(
        String memberId,
        String tokenId,
        Instant expiresAt
) {
}
