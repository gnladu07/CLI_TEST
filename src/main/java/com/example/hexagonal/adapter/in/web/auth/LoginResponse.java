package com.example.hexagonal.adapter.in.web.auth;

import com.example.hexagonal.application.port.in.LoginResult;

/**
 * @param expiresIn Access Token 만료까지 남은 시간(초)
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {

    private static final String BEARER_TOKEN_TYPE = "Bearer";

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken(), BEARER_TOKEN_TYPE, result.expiresIn().toSeconds());
    }

    @Override
    public String toString() {
        return "LoginResponse[accessToken=****, tokenType=" + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}
