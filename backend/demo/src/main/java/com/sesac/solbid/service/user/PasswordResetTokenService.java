package com.sesac.solbid.service.user;

public interface PasswordResetTokenService {
    String createToken(String email);
    String consumeToken(String token); // 반환: email (검증 + 1회성 소비)
    boolean validateToken(String token);
    String getEmailIfValid(String token);
}
