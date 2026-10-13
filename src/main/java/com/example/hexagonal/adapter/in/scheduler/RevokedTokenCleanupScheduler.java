package com.example.hexagonal.adapter.in.scheduler;

import com.example.hexagonal.application.port.in.PurgeExpiredRevokedTokensUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevokedTokenCleanupScheduler {

    private final PurgeExpiredRevokedTokensUseCase purgeExpiredRevokedTokensUseCase;

    @Scheduled(cron = "${jwt.revoked-token-cleanup-cron}")
    public void purgeExpiredRevokedTokens() {
        log.info("RevokedTokenCleanupScheduler : purgeExpiredRevokedTokens : 만료된 무효화 토큰 정리 작업 실행");
        try {
            purgeExpiredRevokedTokensUseCase.purgeExpired();
        } catch (RuntimeException e) {
            log.error("RevokedTokenCleanupScheduler : purgeExpiredRevokedTokens : 만료된 무효화 토큰 정리 실패", e);
        }
    }
}
