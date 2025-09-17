package com.sesac.solbid.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordResetExceptionUtils 테스트")
class PasswordResetExceptionUtilsTest {

    private static final String TEST_EMAIL = "test@example.com";

    @Test
    @DisplayName("유효하지 않은 OTP 예외 생성")
    void createInvalidOtpException() {
        // when
        PasswordResetException exception = PasswordResetExceptionUtils.invalidOtp(TEST_EMAIL);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_RESET_OTP_INVALID);
        assertThat(exception.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(exception.getAdditionalInfo()).isNull();
    }

    @Test
    @DisplayName("만료된 OTP 예외 생성")
    void createExpiredOtpException() {
        // when
        PasswordResetException exception = PasswordResetExceptionUtils.expiredOtp(TEST_EMAIL);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_RESET_OTP_EXPIRED);
        assertThat(exception.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(exception.getAdditionalInfo()).isNull();
    }

    @Test
    @DisplayName("시도 횟수 초과 예외 생성")
    void createAttemptsExceededException() {
        // given
        int maxAttempts = 3;

        // when
        PasswordResetException exception = PasswordResetExceptionUtils.attemptsExceeded(TEST_EMAIL, maxAttempts);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_RESET_OTP_ATTEMPTS_EXCEEDED);
        assertThat(exception.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(exception.getAdditionalInfo()).isEqualTo("최대 3회 시도 가능");
    }

    @Test
    @DisplayName("재전송 횟수 제한 초과 예외 생성")
    void createResendLimitExceededException() {
        // given
        int currentCount = 3;
        int maxCount = 5;

        // when
        PasswordResetException exception = PasswordResetExceptionUtils.resendLimitExceeded(TEST_EMAIL, currentCount, maxCount);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_RESET_RESEND_LIMIT_EXCEEDED);
        assertThat(exception.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(exception.getAdditionalInfo()).isEqualTo("현재 3회, 최대 5회");
    }

    @Test
    @DisplayName("재전송 간격 제한 예외 생성")
    void createResendTooFrequentException() {
        // given
        long remainingSeconds = 45;

        // when
        PasswordResetException exception = PasswordResetExceptionUtils.resendTooFrequent(TEST_EMAIL, remainingSeconds);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_RESET_RESEND_TOO_FREQUENT);
        assertThat(exception.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(exception.getAdditionalInfo()).isEqualTo("45초 후 재시도 가능");
    }

    @Test
    @DisplayName("사용자 없음 예외 생성")
    void createUserNotFoundException() {
        // when
        PasswordResetException exception = PasswordResetExceptionUtils.userNotFound(TEST_EMAIL);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        assertThat(exception.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(exception.getAdditionalInfo()).isNull();
    }

    @Test
    @DisplayName("재설정 불가 예외 생성")
    void createResetNotAllowedException() {
        // when
        PasswordResetException exception = PasswordResetExceptionUtils.resetNotAllowed(TEST_EMAIL);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_RESET_NOT_ALLOWED);
        assertThat(exception.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(exception.getAdditionalInfo()).isNull();
    }

    @Test
    @DisplayName("이전 비밀번호와 동일 예외 생성")
    void createSameAsOldPasswordException() {
        // when
        PasswordResetException exception = PasswordResetExceptionUtils.sameAsOldPassword(TEST_EMAIL);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_RESET_SAME_AS_OLD);
        assertThat(exception.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(exception.getAdditionalInfo()).isNull();
    }
}