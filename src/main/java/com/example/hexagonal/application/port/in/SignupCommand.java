package com.example.hexagonal.application.port.in;

public record SignupCommand(
        String loginId,
        String password,
        String name,
        int age,
        String email
) {

    @Override
    public String toString() {
        return "SignupCommand[loginId=" + loginId + ", password=****, name=" + name
                + ", age=" + age + ", email=****]";
    }
}
