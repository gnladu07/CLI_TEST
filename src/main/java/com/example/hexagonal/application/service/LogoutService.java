package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.in.LogoutCommand;
import com.example.hexagonal.application.port.in.LogoutUseCase;
import com.example.hexagonal.application.port.out.SaveRevokedTokenPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final SaveRevokedTokenPort saveRevokedTokenPort;

    @Override
    @Transactional
    public void logout(LogoutCommand command) {
        log.info("LogoutService : logout : 로그아웃 비즈니스 로직 시작 - memberId={}", command.memberId());
        log.debug(
                "LogoutService : logout : 무효화 대상 토큰 확인 - tokenId={}, expiresAt={}",
                command.tokenId(),
                command.expiresAt()
        );

        saveRevokedTokenPort.save(command.tokenId(), command.expiresAt());

        log.info("LogoutService : logout : 로그아웃 완료 - memberId={}", command.memberId());
    }
}
