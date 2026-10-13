package com.example.hexagonal.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.hexagonal.application.port.out.DeleteRevokedTokenPort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RevokedTokenPurgeServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-13T06:00:00Z");

    private final DeleteRevokedTokenPort deleteRevokedTokenPort = mock(DeleteRevokedTokenPort.class);
    private final RevokedTokenPurgeService purgeService =
            new RevokedTokenPurgeService(deleteRevokedTokenPort, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void 현재_시각_기준으로_만료된_무효화_토큰을_삭제한다() {
        given(deleteRevokedTokenPort.deleteExpiredBefore(NOW)).willReturn(3);

        int deletedCount = purgeService.purgeExpired();

        assertThat(deletedCount).isEqualTo(3);
    }
}
