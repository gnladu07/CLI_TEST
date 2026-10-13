package com.example.hexagonal.adapter.in.web.auth;

import com.example.hexagonal.application.port.in.LoginUseCase;
import com.example.hexagonal.application.port.in.LogoutCommand;
import com.example.hexagonal.application.port.in.LogoutUseCase;
import com.example.hexagonal.application.port.in.SignupUseCase;
import com.example.hexagonal.common.security.jwt.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("AuthController : POST signup : 회원가입 요청");

        Long memberId = signupUseCase.signup(request.toCommand());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SignupResponse(memberId, request.loginId()));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("AuthController : POST login : 로그인 요청");

        LoginResponse response = LoginResponse.from(loginUseCase.login(request.toCommand()));

        return ResponseEntity.ok(response);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal JwtPrincipal principal) {
        log.info("AuthController : POST logout : 로그아웃 요청");

        logoutUseCase.logout(new LogoutCommand(principal.subject(), principal.tokenId(), principal.expiresAt()));

        return ResponseEntity.noContent().build();
    }
}
