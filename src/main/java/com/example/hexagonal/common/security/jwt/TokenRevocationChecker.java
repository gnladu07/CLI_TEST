package com.example.hexagonal.common.security.jwt;

public interface TokenRevocationChecker {

    /**
     * 로그아웃 등으로 무효화된 토큰인지 확인한다.
     */
    boolean isRevoked(String tokenId);
}
