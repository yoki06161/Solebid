package com.sesac.solbid.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordResetException 테스트")
class PasswordResetExceptionTest {

    @Test
    @DisplayName("기본 생성자로 예외 생성")
    void createExceptionWithBasicConstructor() {
        // given
        String email = "test@example.com";
        ErrorCode errorCode = ErrorCode.PASSWORD_RESET_OTP_INVALID;

        // when
        PasswordResetException exception = new PasswordResetException(errorCode, email);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getEmail()).isEqualTo(email);
        assertThat(exception.getAdditionalInfo()).isNull();
    }

    @Test
    @DisplayName("추가 정보와 함께 예외 생성")
    void createExceptionWithAdditionalInfo() {
        // given
        String email = "test@example.com";
        ErrorCode errorCode = ErrorCode.PASSWORD_RESET_RESEND_LIMIT_EXCEEDED;
        String additionalInfo = "현재 3회, 최대 5회";

        // when
        PasswordResetException exception = new PasswordResetException(errorCode, email, additionalInfo);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getEmail()).isEqualTo(email);
        assertThat(exception.getAdditionalInfo()).isEqualTo(additionalInfo);
    }

    @Test
    @DisplayName("이메일 마스킹 - 일반적인 이메일")
    void maskEmailNormal() {
        // given
        String email = "testuser@example.com";
        PasswordResetException exception = new PasswordResetException(ErrorCode.PASSWORD_RESET_OTP_INVALID, email);

        // when
        String maskedEmail = exception.getMaskedEmail();

        // then
        assertThat(maskedEmail).isEqualTo("te****@example.com");
    }

    @Test
    @DisplayName("이메일 마스킹 - 짧은 이메일")
    void maskEmailShort() {
        // given
        String email = "ab@example.com";
        PasswordResetException exception = new PasswordResetException(ErrorCode.PASSWORD_RESET_OTP_INVALID, email);

        // when
        String maskedEmail = exception.getMaskedEmail();

        // then
        assertThat(maskedEmail).isEqualTo("**@example.com");
    }

    @Test
    @DisplayName("이메일 마스킹 - null 이메일")
    void maskEmailNull() {
        // given
        PasswordResetException exception = new PasswordResetException(ErrorCode.PASSWORD_RESET_OTP_INVALID, null);

        // when
        String maskedEmail = exception.getMaskedEmail();

        // then
        assertThat(maskedEmail).isEqualTo("****");
    }

    @Test
    @DisplayName("이메일 마스킹 - 잘못된 형식의 이메일")
    void maskEmailInvalidFormat() {
        // given
        String email = "invalid-email";
        PasswordResetException exception = new PasswordResetException(ErrorCode.PASSWORD_RESET_OTP_INVALID, email);

        // when
        String maskedEmail = exception.getMaskedEmail();

        // then
        assertThat(maskedEmail).isEqualTo("****");
    }
}