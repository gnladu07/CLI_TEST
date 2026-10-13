package com.example.hexagonal.application.port.in;

public record LoginCommand(
        String loginId,
        String password
) {

    @Override
    public String toString() {
        return "LoginCommand[loginId=" + loginId + ", password=****]";
    }
}
