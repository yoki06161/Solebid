package com.sesac.solbid.dto.auth.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("PasswordResetVerifyRequest - 유효한 입력값은 검증을 통과해야 한다")
    void passwordResetVerifyRequest_validInput_shouldPassValidation() {
        // Given
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPassword123");

        // When
        Set<ConstraintViolation<PasswordResetVerifyRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("PasswordResetVerifyRequest - 잘못된 이메일 형식은 검증에 실패해야 한다")
    void passwordResetVerifyRequest_invalidEmail_shouldFailValidation() {
        // Given
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest();
        request.setEmail("invalid-email");
        request.setOtp("123456");
        request.setNewPassword("newPassword123");

        // When
        Set<ConstraintViolation<PasswordResetVerifyRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("이메일 형식이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("PasswordResetVerifyRequest - 잘못된 OTP 형식은 검증에 실패해야 한다")
    void passwordResetVerifyRequest_invalidOtp_shouldFailValidation() {
        // Given
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest();
        request.setEmail("test@example.com");
        request.setOtp("12345"); // 5자리만
        request.setNewPassword("newPassword123");

        // When
        Set<ConstraintViolation<PasswordResetVerifyRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("인증번호는 6자리 숫자여야 합니다.");
    }

    @Test
    @DisplayName("PasswordResetVerifyRequest - 짧은 비밀번호는 검증에 실패해야 한다")
    void passwordResetVerifyRequest_shortPassword_shouldFailValidation() {
        // Given
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("short"); // 8자 미만

        // When
        Set<ConstraintViolation<PasswordResetVerifyRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("비밀번호는 8자 이상 64자 이하여야 합니다.");
    }

    @Test
    @DisplayName("ResendOtpRequest - 유효한 입력값은 검증을 통과해야 한다")
    void resendOtpRequest_validInput_shouldPassValidation() {
        // Given
        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<ResendOtpRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("ResendOtpRequest - 잘못된 이메일 형식은 검증에 실패해야 한다")
    void resendOtpRequest_invalidEmail_shouldFailValidation() {
        // Given
        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("invalid-email");

        // When
        Set<ConstraintViolation<ResendOtpRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("이메일 형식이 올바르지 않습니다.");
    }
}