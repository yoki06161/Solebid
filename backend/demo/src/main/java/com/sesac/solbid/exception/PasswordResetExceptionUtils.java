package com.sesac.solbid.exception;

/**
 * 비밀번호 재설정 예외 처리 유틸리티 클래스
 * 비밀번호 재설정 관련 예외를 생성하고 관리하는 유틸리티 메서드들을 제공합니다.
 */
public class PasswordResetExceptionUtils {

    /**
     * 비밀번호 재설정 OTP가 유효하지 않을 때 예외를 생성합니다.
     */
    public static PasswordResetException invalidOtp(String email) {
        return new PasswordResetException(ErrorCode.PASSWORD_RESET_OTP_INVALID, email);
    }

    /**
     * 비밀번호 재설정 OTP가 만료되었을 때 예외를 생성합니다.
     */
    public static PasswordResetException expiredOtp(String email) {
        return new PasswordResetException(ErrorCode.PASSWORD_RESET_OTP_EXPIRED, email);
    }

    /**
     * 비밀번호 재설정 OTP 시도 횟수를 초과했을 때 예외를 생성합니다.
     */
    public static PasswordResetException attemptsExceeded(String email, int maxAttempts) {
        String additionalInfo = String.format("최대 %d회 시도 가능", maxAttempts);
        return new PasswordResetException(ErrorCode.PASSWORD_RESET_OTP_ATTEMPTS_EXCEEDED, email, additionalInfo);
    }

    /**
     * 재전송 횟수 제한을 초과했을 때 예외를 생성합니다.
     */
    public static PasswordResetException resendLimitExceeded(String email, int currentCount, int maxCount) {
        String additionalInfo = String.format("현재 %d회, 최대 %d회", currentCount, maxCount);
        return new PasswordResetException(ErrorCode.PASSWORD_RESET_RESEND_LIMIT_EXCEEDED, email, additionalInfo);
    }

    /**
     * 재전송 간격 제한에 걸렸을 때 예외를 생성합니다.
     */
    public static PasswordResetException resendTooFrequent(String email, long remainingSeconds) {
        String additionalInfo = String.format("%d초 후 재시도 가능", remainingSeconds);
        return new PasswordResetException(ErrorCode.PASSWORD_RESET_RESEND_TOO_FREQUENT, email, additionalInfo);
    }

    /**
     * 사용자를 찾을 수 없을 때 예외를 생성합니다.
     */
    public static PasswordResetException userNotFound(String email) {
        return new PasswordResetException(ErrorCode.USER_NOT_FOUND, email);
    }

    /**
     * 비밀번호 재설정이 허용되지 않을 때 예외를 생성합니다.
     */
    public static PasswordResetException resetNotAllowed(String email) {
        return new PasswordResetException(ErrorCode.PASSWORD_RESET_NOT_ALLOWED, email);
    }

    /**
     * 이전 비밀번호와 동일할 때 예외를 생성합니다.
     */
    public static PasswordResetException sameAsOldPassword(String email) {
        return new PasswordResetException(ErrorCode.PASSWORD_RESET_SAME_AS_OLD, email);
    }
}