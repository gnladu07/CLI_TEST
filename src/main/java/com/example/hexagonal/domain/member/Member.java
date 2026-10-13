package com.example.hexagonal.domain.member;

import java.util.Objects;

public class Member {

    private final Long id;
    private final String loginId;
    private final String encodedPassword;
    private final String name;
    private final int age;
    private final String email;
    private final MemberRole role;

    private Member(
            Long id,
            String loginId,
            String encodedPassword,
            String name,
            int age,
            String email,
            MemberRole role
    ) {
        this.id = id;
        this.loginId = Objects.requireNonNull(loginId, "loginId must not be null");
        this.encodedPassword = Objects.requireNonNull(encodedPassword, "encodedPassword must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.age = age;
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
    }

    /**
     * 신규 회원을 생성한다. 비밀번호는 반드시 암호화된 값을 전달해야 하며, 신규 회원은 USER 권한을 가진다.
     */
    public static Member register(String loginId, String encodedPassword, String name, int age, String email) {
        return new Member(null, loginId, encodedPassword, name, age, email, MemberRole.USER);
    }

    public static Member restore(
            Long id,
            String loginId,
            String encodedPassword,
            String name,
            int age,
            String email,
            MemberRole role
    ) {
        return new Member(Objects.requireNonNull(id, "id must not be null"),
                loginId, encodedPassword, name, age, email, role);
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public MemberRole getRole() {
        return role;
    }
}
