package com.example.hexagonal.common.exception;

public record ErrorResponse(
        int status,
        String code,
        String message
) {
}
