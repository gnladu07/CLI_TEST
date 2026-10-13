package com.example.hexagonal.application.port.in;

public interface SignupUseCase {

    /**
     * 회원가입을 수행하고 생성된 회원 ID를 반환한다.
     */
    Long signup(SignupCommand command);
}
