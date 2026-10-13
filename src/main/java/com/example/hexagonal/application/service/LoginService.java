package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.in.LoginCommand;
import com.example.hexagonal.application.port.in.LoginResult;
import com.example.hexagonal.application.port.in.LoginUseCase;
import com.example.hexagonal.application.port.out.IssueTokenPort;
import com.example.hexagonal.application.port.out.IssuedAccessToken;
import com.example.hexagonal.application.port.out.LoadMemberPort;
import com.example.hexagonal.common.exception.BusinessException;
import com.example.hexagonal.common.exception.ErrorCode;
import com.example.hexagonal.domain.member.Member;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LoginService implements LoginUseCase {

    private final LoadMemberPort loadMemberPort;
    private final PasswordEncoder passwordEncoder;
    private final IssueTokenPort issueTokenPort;

    // 존재하지 않는 아이디에도 동일하게 해시 비교를 수행해 응답 시간으로 가입 여부가 드러나지 않게 한다.
    private final String dummyEncodedPassword;

    public LoginService(LoadMemberPort loadMemberPort, PasswordEncoder passwordEncoder, IssueTokenPort issueTokenPort) {
        this.loadMemberPort = loadMemberPort;
        this.passwordEncoder = passwordEncoder;
        this.issueTokenPort = issueTokenPort;
        this.dummyEncodedPassword = passwordEncoder.encode("dummy-password-for-login-timing");
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        log.info("LoginService : login : 로그인 비즈니스 로직 시작");
        log.debug("LoginService : login : 로그인 요청 데이터 확인 - loginId={}", command.loginId());

        Member member = authenticate(command);
        IssuedAccessToken accessToken = issueTokenPort.issueAccessToken(member);

        log.info(
                "LoginService : login : 로그인 완료 - memberId={}, loginId={}",
                member.getId(),
                member.getLoginId()
        );
        return new LoginResult(accessToken.value(), accessToken.expiresIn());
    }

    private Member authenticate(LoginCommand command) {
        Optional<Member> member = loadMemberPort.findByLoginId(command.loginId());
        String encodedPassword = member.map(Member::getEncodedPassword).orElse(dummyEncodedPassword);
        boolean passwordMatches = passwordEncoder.matches(command.password(), encodedPassword);

        if (member.isEmpty() || !passwordMatches) {
            log.warn(
                    "LoginService : authenticate : 로그인 인증 실패 - loginId={}, reason={}",
                    command.loginId(),
                    member.isEmpty() ? "MEMBER_NOT_FOUND" : "PASSWORD_MISMATCH"
            );
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return member.get();
    }
}
