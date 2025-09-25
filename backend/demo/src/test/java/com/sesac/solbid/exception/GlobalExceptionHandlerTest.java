package com.sesac.solbid.exception;

import com.sesac.solbid.dto.api.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GlobalExceptionHandler 테스트
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleEmailVerificationExceptionWithBasicError() {
        // Given
        String email = "test@example.com";
        EmailVerificationException exception = EmailVerificationExceptionUtils.alreadyVerified(email);

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleEmailVerificationException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("EMAIL_ALREADY_VERIFIED", response.getBody().getErrorCode());
        assertEquals("이미 인증된 이메일입니다.", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void testHandleEmailVerificationExceptionWithResendTooFrequent() {
        // Given
        String email = "test@example.com";
        EmailVerificationException exception = EmailVerificationExceptionUtils.resendTooFrequent(email, 180);

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleEmailVerificationException(exception);

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("EMAIL_VERIFICATION_RESEND_TOO_FREQUENT", response.getBody().getErrorCode());
        assertEquals("인증번호 재전송은 1분 후에 가능합니다.", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void testHandleEmailVerificationExceptionWithDailyLimitExceeded() {
        // Given
        String email = "test@example.com";
        EmailVerificationException exception = EmailVerificationExceptionUtils.resendLimitExceeded(email, 5, 5);

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleEmailVerificationException(exception);

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("EMAIL_VERIFICATION_RESEND_LIMIT_EXCEEDED", response.getBody().getErrorCode());
        assertEquals("인증 이메일 재전송 횟수를 초과했습니다.", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void testHandlePasswordResetExceptionWithBasicError() {
        // Given
        String email = "test@example.com";
        PasswordResetException exception = PasswordResetExceptionUtils.invalidOtp(email);

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handlePasswordResetException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("PASSWORD_RESET_OTP_INVALID", response.getBody().getErrorCode());
        assertEquals("비밀번호 재설정 인증번호가 유효하지 않습니다.", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void testHandlePasswordResetExceptionWithResendTooFrequent() {
        // Given
        String email = "test@example.com";
        PasswordResetException exception = PasswordResetExceptionUtils.resendTooFrequent(email, 45);

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handlePasswordResetException(exception);

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("PASSWORD_RESET_RESEND_TOO_FREQUENT", response.getBody().getErrorCode());
        assertEquals("인증번호 재전송은 1분 후에 가능합니다.", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void testHandlePasswordResetExceptionWithResendLimitExceeded() {
        // Given
        String email = "test@example.com";
        PasswordResetException exception = PasswordResetExceptionUtils.resendLimitExceeded(email, 5, 5);

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handlePasswordResetException(exception);

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("PASSWORD_RESET_RESEND_LIMIT_EXCEEDED", response.getBody().getErrorCode());
        assertEquals("재전송 횟수를 초과했습니다.", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
    }

    @Test
    void testHandleCustomException() {
        // Given
        CustomException exception = new CustomException(ErrorCode.USER_NOT_FOUND);

        // When
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleCustomException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("USER_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("사용자를 찾을 수 없습니다.", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}