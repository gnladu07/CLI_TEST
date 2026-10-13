package com.example.hexagonal.application.port.in;

import java.time.Duration;

public record LoginResult(
        String accessToken,
        Duration expiresIn
) {

    @Override
    public String toString() {
        return "LoginResult[accessToken=****, expiresIn=" + expiresIn + "]";
    }
}
