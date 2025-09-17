package com.sesac.solbid.service.user;

/**
 * 이메일 인증 토큰 관리 서비스 인터페이스
 * 토큰 생성, 검증, 소비, 만료 처리 및 재전송 제한 기능을 제공합니다.
 */
public interface EmailVerificationTokenService {
    
    /**
     * 이메일 인증 토큰을 생성합니다.
     * @param email 인증할 이메일 주소
     * @return 생성된 토큰
     */
    String createToken(String email);
    
    /**
     * 토큰을 검증하고 유효한 경우 이메일을 반환합니다.
     * @param token 검증할 토큰
     * @return 유효한 경우 이메일, 무효한 경우 null
     */
    String getEmailIfValid(String token);
    
    /**
     * 토큰을 소비합니다 (일회성 사용).
     * @param token 소비할 토큰
     * @return 유효한 경우 이메일, 무효한 경우 null
     */
    String consumeToken(String token);
    
    /**
     * 재전송 요청이 가능한지 확인합니다.
     * @param email 확인할 이메일 주소
     * @return 재전송 가능 여부
     */
    boolean canRequestResend(String email);
    
    /**
     * 재전송 요청을 기록합니다.
     * @param email 재전송 요청한 이메일 주소
     */
    void recordResendRequest(String email);
    
    /**
     * 일일 재전송 횟수를 확인합니다.
     * @param email 확인할 이메일 주소
     * @return 오늘 재전송한 횟수
     */
    int getDailyResendCount(String email);
    
    /**
     * 마지막 재전송 시간을 확인합니다.
     * @param email 확인할 이메일 주소
     * @return 마지막 재전송 시간 (초 단위), 없으면 -1
     */
    long getLastResendTime(String email);
    
    /**
     * 유효한 토큰이 존재하는지 확인합니다.
     * @param email 확인할 이메일 주소
     * @return 유효한 토큰 존재 여부
     */
    boolean hasValidToken(String email);
    
    /**
     * 토큰의 남은 유효 시간을 초 단위로 반환합니다.
     * @param email 확인할 이메일 주소
     * @return 남은 시간 (초), 토큰이 없거나 만료된 경우 0
     */
    long getRemainingTimeSeconds(String email);
    
    /**
     * 토큰의 남은 시도 횟수를 반환합니다.
     * @param email 확인할 이메일 주소
     * @return 남은 시도 횟수, 토큰이 없는 경우 0
     */
    int getRemainingAttempts(String email);
}