package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.in.SignupCommand;
import com.example.hexagonal.application.port.in.SignupUseCase;
import com.example.hexagonal.application.port.out.LoadMemberPort;
import com.example.hexagonal.application.port.out.SaveMemberPort;
import com.example.hexagonal.common.exception.BusinessException;
import com.example.hexagonal.common.exception.ErrorCode;
import com.example.hexagonal.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Long signup(SignupCommand command) {
        log.info("SignupService : signup : 회원가입 비즈니스 로직 시작");
        log.debug("SignupService : signup : 회원가입 요청 데이터 확인 - loginId={}", command.loginId());

        validateDuplication(command);

        Member member = Member.register(
                command.loginId(),
                passwordEncoder.encode(command.password()),
                command.name(),
                command.age(),
                command.email()
        );
        Member savedMember = saveMemberPort.save(member);

        log.info(
                "SignupService : signup : 회원가입 완료 - memberId={}, loginId={}",
                savedMember.getId(),
                savedMember.getLoginId()
        );
        return savedMember.getId();
    }

    private void validateDuplication(SignupCommand command) {
        if (loadMemberPort.existsByLoginId(command.loginId())) {
            log.warn("SignupService : validateDuplication : 아이디 중복 - loginId={}", command.loginId());
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (loadMemberPort.existsByEmail(command.email())) {
            log.warn("SignupService : validateDuplication : 이메일 중복 - loginId={}", command.loginId());
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
