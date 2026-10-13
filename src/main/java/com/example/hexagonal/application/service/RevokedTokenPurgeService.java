package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.in.PurgeExpiredRevokedTokensUseCase;
import com.example.hexagonal.application.port.out.DeleteRevokedTokenPort;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevokedTokenPurgeService implements PurgeExpiredRevokedTokensUseCase {

    private final DeleteRevokedTokenPort deleteRevokedTokenPort;
    private final Clock clock;

    @Override
    @Transactional
    public int purgeExpired() {
        Instant now = Instant.now(clock);
        log.debug("RevokedTokenPurgeService : purgeExpired : 만료된 무효화 토큰 정리 시작 - now={}", now);

        int deletedCount = deleteRevokedTokenPort.deleteExpiredBefore(now);

        log.info("RevokedTokenPurgeService : purgeExpired : 만료된 무효화 토큰 정리 완료 - deletedCount={}", deletedCount);
        return deletedCount;
    }
}
