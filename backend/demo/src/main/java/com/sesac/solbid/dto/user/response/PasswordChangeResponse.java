package com.sesac.solbid.dto.user.response;

/**
 * 비밀번호 변경 응답 DTO
 * <p>
 * 비밀번호 변경 성공 시 반환되는 정보를 담는 DTO입니다.
 * </p>
 * 
 * @param message 변경 완료 메시지
 * @param sessionInvalidated 세션 무효화 여부
 * @param notificationSent 알림 발송 여부
 */
public record PasswordChangeResponse(
    String message,
    boolean sessionInvalidated,
    boolean notificationSent
) {
    /**
     * 기본 비밀번호 변경 성공 응답을 생성합니다.
     * 
     * @return 기본 성공 응답
     */
    public static PasswordChangeResponse success() {
        return new PasswordChangeResponse(
            "비밀번호가 성공적으로 변경되었습니다.",
            true,
            true
        );
    }
    
    /**
     * 커스텀 메시지와 함께 비밀번호 변경 성공 응답을 생성합니다.
     * 
     * @param customMessage 커스텀 메시지
     * @param sessionInvalidated 세션 무효화 여부
     * @param notificationSent 알림 발송 여부
     * @return 커스텀 성공 응답
     */
    public static PasswordChangeResponse success(String customMessage, boolean sessionInvalidated, boolean notificationSent) {
        return new PasswordChangeResponse(customMessage, sessionInvalidated, notificationSent);
    }
}