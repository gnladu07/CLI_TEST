package com.example.hexagonal.adapter.in.web.auth;

import com.example.hexagonal.adapter.in.web.validation.MaxUtf8Bytes;
import com.example.hexagonal.application.port.in.SignupCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "아이디는 필수입니다.")
        @Pattern(regexp = "^[a-z0-9]{4,20}$", message = "아이디는 영문 소문자와 숫자 4~20자여야 합니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함한 8~20자여야 하며 공백을 포함할 수 없습니다."
        )
        // BCrypt는 72바이트까지만 비교하므로 그 이후 문자가 인증에서 무시되지 않도록 제한한다.
        @MaxUtf8Bytes(value = 72, message = "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.")
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
        String name,

        @NotNull(message = "나이는 필수입니다.")
        @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
        @Max(value = 150, message = "나이는 150 이하여야 합니다.")
        Integer age,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
        String email
) {

    public SignupCommand toCommand() {
        return new SignupCommand(loginId, password, name.strip(), age, email);
    }

    @Override
    public String toString() {
        return "SignupRequest[loginId=" + loginId + ", password=****, name=" + name
                + ", age=" + age + ", email=****]";
    }
}
