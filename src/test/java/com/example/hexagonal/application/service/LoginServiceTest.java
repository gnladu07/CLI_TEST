package com.example.hexagonal.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.hexagonal.application.port.in.LoginCommand;
import com.example.hexagonal.application.port.in.LoginResult;
import com.example.hexagonal.application.port.out.IssueTokenPort;
import com.example.hexagonal.application.port.out.IssuedAccessToken;
import com.example.hexagonal.application.port.out.LoadMemberPort;
import com.example.hexagonal.common.exception.BusinessException;
import com.example.hexagonal.common.exception.ErrorCode;
import com.example.hexagonal.domain.member.Member;
import com.example.hexagonal.domain.member.MemberRole;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final LoginCommand COMMAND = new LoginCommand("tester01", "Password1!");
    private static final Member MEMBER = Member.restore(
            1L, "tester01", "encoded-password", "홍길동", 25, "tester01@example.com", MemberRole.USER);

    @Mock
    private LoadMemberPort loadMemberPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IssueTokenPort issueTokenPort;

    @InjectMocks
    private LoginService loginService;

    @Test
    void 아이디와_비밀번호가_일치하면_Access_Token을_발급한다() {
        given(loadMemberPort.findByLoginId("tester01")).willReturn(Optional.of(MEMBER));
        given(passwordEncoder.matches("Password1!", "encoded-password")).willReturn(true);
        given(issueTokenPort.issueAccessToken(MEMBER))
                .willReturn(new IssuedAccessToken("access-token", Duration.ofMinutes(30)));

        LoginResult result = loginService.login(COMMAND);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.expiresIn()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void 비밀번호가_틀리면_INVALID_CREDENTIALS_예외를_던진다() {
        given(loadMemberPort.findByLoginId("tester01")).willReturn(Optional.of(MEMBER));
        given(passwordEncoder.matches("Password1!", "encoded-password")).willReturn(false);

        assertThatThrownBy(() -> loginService.login(COMMAND))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(issueTokenPort, never()).issueAccessToken(any());
    }

    @Test
    void 존재하지_않는_아이디도_비밀번호_비교를_수행한_뒤_INVALID_CREDENTIALS_예외를_던진다() {
        given(loadMemberPort.findByLoginId("tester01")).willReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(COMMAND))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verify(passwordEncoder).matches(eq("Password1!"), any());
        verify(issueTokenPort, never()).issueAccessToken(any());
    }
}
