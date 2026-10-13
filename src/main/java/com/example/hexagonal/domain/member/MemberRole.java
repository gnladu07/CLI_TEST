package com.example.hexagonal.domain.member;

public enum MemberRole {

    USER("ROLE_USER");

    private final String authority;

    MemberRole(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
