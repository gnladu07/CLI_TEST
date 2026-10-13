package com.example.hexagonal.common.security.jwt;

import com.example.hexagonal.common.security.SecurityErrorCode;
import lombok.Getter;

@Getter
public class InvalidJwtException extends RuntimeException {

    private final SecurityErrorCode errorCode;

    public InvalidJwtException(SecurityErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public InvalidJwtException(SecurityErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
