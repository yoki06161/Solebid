package com.sesac.solbid.service;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.SocialLoginRepository;
import com.sesac.solbid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenService tokenService;
    private final ResetMailService resetMailService;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Transactional
    public void requestReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 소셜 로그인 사용자이면서 비밀번호가 없는 경우 비밀번호 재설정 불가
        if ((user.getPassword() == null || user.getPassword().isBlank()) && socialLoginRepository.findByUser(user).isPresent()) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_NOT_ALLOWED);
        }

        String token = tokenService.createToken(email);
        String link = frontendBaseUrl + "/reset-password?token=" + token;
        resetMailService.sendPasswordResetMail(email, link);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = tokenService.getEmailIfValid(token);
        if (email == null) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getPassword() != null && passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_SAME_AS_OLD);
        }

        user.updatePassword(passwordEncoder.encode(newPassword));

        String consumedEmail = tokenService.consumeToken(token);
        if (consumedEmail == null) {
            throw new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }
    }
}
