package com.example.hexagonal.application.port.in;

public interface LoginUseCase {

    /**
     * 아이디와 비밀번호를 검증하고 Access Token을 발급한다.
     * 아이디가 없거나 비밀번호가 틀리면 INVALID_CREDENTIALS BusinessException을 던진다.
     */
    LoginResult login(LoginCommand command);
}
