package com.example.hexagonal.common.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SecurityErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    REVOKED_TOKEN(HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    /**
     * JWT 필터가 토큰 검증 실패 사유를 AuthenticationEntryPoint에 전달할 때 사용하는 요청 속성 키.
     */
    public static final String REQUEST_ATTRIBUTE = SecurityErrorCode.class.getName();

    private final HttpStatus status;
    private final String message;
}
