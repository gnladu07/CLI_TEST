package com.example.hexagonal.application.port.in;

public interface PurgeExpiredRevokedTokensUseCase {

    /**
     * 만료 시각이 지난 무효화 토큰 기록을 삭제하고 삭제한 건수를 반환한다.
     * 만료된 토큰은 서명 검증 단계에서 거부되므로 무효화 기록이 더 이상 필요하지 않다.
     */
    int purgeExpired();
}
