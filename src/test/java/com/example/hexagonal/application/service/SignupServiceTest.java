package com.example.hexagonal.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.hexagonal.application.port.in.SignupCommand;
import com.example.hexagonal.application.port.out.LoadMemberPort;
import com.example.hexagonal.application.port.out.SaveMemberPort;
import com.example.hexagonal.common.exception.BusinessException;
import com.example.hexagonal.common.exception.ErrorCode;
import com.example.hexagonal.domain.member.Member;
import com.example.hexagonal.domain.member.MemberRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    private static final SignupCommand COMMAND =
            new SignupCommand("tester01", "Password1!", "홍길동", 25, "tester01@example.com");

    @Mock
    private LoadMemberPort loadMemberPort;

    @Mock
    private SaveMemberPort saveMemberPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupService signupService;

    @Test
    void 비밀번호를_암호화하고_USER_권한으로_회원을_저장한다() {
        given(passwordEncoder.encode("Password1!")).willReturn("encoded-password");
        given(saveMemberPort.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            return Member.restore(1L, member.getLoginId(), member.getEncodedPassword(),
                    member.getName(), member.getAge(), member.getEmail(), member.getRole());
        });

        Long memberId = signupService.signup(COMMAND);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(saveMemberPort).save(captor.capture());
        Member saved = captor.getValue();

        assertThat(memberId).isEqualTo(1L);
        assertThat(saved.getLoginId()).isEqualTo("tester01");
        assertThat(saved.getEncodedPassword()).isEqualTo("encoded-password");
        assertThat(saved.getName()).isEqualTo("홍길동");
        assertThat(saved.getAge()).isEqualTo(25);
        assertThat(saved.getEmail()).isEqualTo("tester01@example.com");
        assertThat(saved.getRole()).isEqualTo(MemberRole.USER);
    }

    @Test
    void 아이디가_중복되면_DUPLICATE_LOGIN_ID_예외를_던진다() {
        given(loadMemberPort.existsByLoginId("tester01")).willReturn(true);

        assertThatThrownBy(() -> signupService.signup(COMMAND))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_LOGIN_ID);
        verify(saveMemberPort, never()).save(any());
    }

    @Test
    void 이메일이_중복되면_DUPLICATE_EMAIL_예외를_던진다() {
        given(loadMemberPort.existsByEmail("tester01@example.com")).willReturn(true);

        assertThatThrownBy(() -> signupService.signup(COMMAND))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
        verify(saveMemberPort, never()).save(any());
    }
}
