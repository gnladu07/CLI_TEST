package com.example.hexagonal.application.port.in;

public interface LogoutUseCase {

    /**
     * 현재 Access Token을 무효화한다. 무효화된 토큰은 만료 전이라도 인증에 사용할 수 없다.
     */
    void logout(LogoutCommand command);
}
