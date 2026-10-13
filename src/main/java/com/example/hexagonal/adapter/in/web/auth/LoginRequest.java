package com.example.hexagonal.adapter.in.web.auth;

import com.example.hexagonal.adapter.in.web.validation.MaxUtf8Bytes;
import com.example.hexagonal.application.port.in.LoginCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "아이디는 필수입니다.")
        @Size(max = 20, message = "아이디는 20자 이하여야 합니다.")
        String loginId,

        // 회원가입 정규식과 같은 Unicode code point 단위로 길이를 센다. @Size는 UTF-16 길이를 세므로 이모지 등이 2자로 계산된다.
        // BCrypt는 72바이트 이후를 무시하므로, 저장된 비밀번호 뒤에 문자를 덧붙인 입력이 일치하지 않도록 바이트 길이도 제한한다.
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = "^.{0,20}$", flags = Pattern.Flag.DOTALL, message = "비밀번호는 20자 이하여야 합니다.")
        @MaxUtf8Bytes(value = 72, message = "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.")
        String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(loginId, password);
    }

    @Override
    public String toString() {
        return "LoginRequest[loginId=" + loginId + ", password=****]";
    }
}
