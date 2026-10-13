package com.example.hexagonal.application.service;

import static org.mockito.Mockito.verify;

import com.example.hexagonal.application.port.in.LogoutCommand;
import com.example.hexagonal.application.port.out.SaveRevokedTokenPort;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private SaveRevokedTokenPort saveRevokedTokenPort;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    void 토큰_ID와_만료_시각으로_토큰을_무효화한다() {
        Instant expiresAt = Instant.parse("2026-10-13T06:30:00Z");

        logoutService.logout(new LogoutCommand("1", "token-id", expiresAt));

        verify(saveRevokedTokenPort).save("token-id", expiresAt);
    }
}
