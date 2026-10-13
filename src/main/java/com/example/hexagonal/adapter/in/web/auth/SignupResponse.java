package com.example.hexagonal.adapter.in.web.auth;

public record SignupResponse(
        Long memberId,
        String loginId
) {
}
