package com.sesac.solbid.service.user;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.EmailVerificationExceptionUtils;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이메일 인증 핵심 서비스
 * 이메일 인증 토큰 생성, 인증 처리, 재전송 요청 등의 핵심 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    private final EmailVerificationTokenService tokenService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    // 재전송 제한 상수
    private static final int RESEND_INTERVAL_MINUTES = 1;
    private static final int DAILY_RESEND_LIMIT = 5;

    /**
     * 인증 이메일을 전송합니다.
     * @param email 인증할 이메일 주소
     */
    @Transactional
    public void sendVerificationEmail(String email) {
        log.info("이메일 인증 메일 전송 시작: {}", maskEmail(email));
        
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 이미 인증된 사용자인지 확인
        if (user.getEmailVerified()) {
            log.warn("이미 인증된 이메일로 인증 메일 전송 시도: {}", maskEmail(email));
            throw EmailVerificationExceptionUtils.alreadyVerified(email);
        }
        
        // 토큰 생성 및 이메일 전송
        String token = tokenService.createToken(email);
        emailService.sendVerificationEmail(email, token);
        
        log.info("이메일 인증 메일 전송 완료: {}", maskEmail(email));
    }

    /**
     * 회원가입 전 이메일 인증번호를 전송합니다.
     * @param email 인증할 이메일 주소
     */
    @Transactional
    public void sendVerificationForSignup(String email) {
        log.info("회원가입 전 이메일 인증번호 전송 시작: {}", maskEmail(email));
        
        // 이미 가입된 이메일인지 확인
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("이미 가입된 이메일로 회원가입 전 인증번호 전송 시도: {}", maskEmail(email));
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        
        // 재전송 제한 확인
        validateResendLimits(email);
        
        // 재전송 요청 기록
        tokenService.recordResendRequest(email);
        
        // 토큰 생성 및 이메일 전송
        String token = tokenService.createToken(email);
        emailService.sendVerificationEmail(email, token);
        
        log.info("회원가입 전 이메일 인증번호 전송 완료: {}", maskEmail(email));
    }

    /**
     * 이메일 인증을 처리합니다. (토큰 방식 - 하위 호환성)
     * @param token 인증 토큰
     * @return 인증된 사용자의 이메일
     */
    @Transactional
    public String verifyEmail(String token) {
        log.info("이메일 인증 처리 시작: token={}", token.substring(0, Math.min(8, token.length())) + "...");
        
        // 토큰 검증 및 소비
        String email = tokenService.consumeToken(token);
        if (email == null) {
            log.warn("유효하지 않은 인증 토큰: {}", token.substring(0, Math.min(8, token.length())) + "...");
            throw EmailVerificationExceptionUtils.invalidToken(null); // 토큰이 유효하지 않으므로 이메일을 알 수 없음
        }
        
        // 사용자 조회 및 인증 처리
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 이미 인증된 경우 처리
        if (user.getEmailVerified()) {
            log.info("이미 인증된 사용자의 토큰 사용: {}", maskEmail(email));
            return email; // 이미 인증된 경우에도 성공으로 처리
        }
        
        // 이메일 인증 처리
        user.verifyEmail();
        
        log.info("이메일 인증 완료: {}", maskEmail(email));
        return email;
    }

    /**
     * 이메일 인증번호를 검증합니다. (새로운 인증번호 방식)
     * @param email 이메일 주소
     * @param verificationCode 6자리 인증번호
     * @return 인증된 사용자의 이메일
     */
    @Transactional
    public String verifyEmailWithCode(String email, String verificationCode) {
        log.info("이메일 인증번호 검증 시작: email={}, code={}", maskEmail(email), maskCode(verificationCode));
        
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 이미 인증된 경우 처리
        if (user.getEmailVerified()) {
            log.info("이미 인증된 사용자의 인증번호 사용: {}", maskEmail(email));
            return email; // 이미 인증된 경우에도 성공으로 처리
        }
        
        // 인증번호 검증 및 소비
        String tokenEmail = tokenService.consumeToken(verificationCode);
        if (tokenEmail == null || !tokenEmail.equals(email)) {
            log.warn("유효하지 않은 인증번호: email={}, code={}", maskEmail(email), maskCode(verificationCode));
            throw EmailVerificationExceptionUtils.invalidToken(email);
        }
        
        // 이메일 인증 처리
        user.verifyEmail();
        
        log.info("이메일 인증번호 검증 완료: {}", maskEmail(email));
        return email;
    }

    /**
     * 회원가입 전 이메일 인증번호를 검증합니다.
     * @param email 이메일 주소
     * @param verificationCode 6자리 인증번호
     * @return 검증된 이메일 주소
     */
    @Transactional
    public String verifyEmailForSignup(String email, String verificationCode) {
        log.info("회원가입 전 이메일 인증번호 검증 시작: email={}, code={}", maskEmail(email), maskCode(verificationCode));
        
        // 인증번호 검증 및 소비
        String tokenEmail = tokenService.consumeToken(verificationCode);
        if (tokenEmail == null || !tokenEmail.equals(email)) {
            log.warn("유효하지 않은 인증번호: email={}, code={}", maskEmail(email), maskCode(verificationCode));
            throw EmailVerificationExceptionUtils.invalidToken(email);
        }
        
        log.info("회원가입 전 이메일 인증번호 검증 완료: {}", maskEmail(email));
        return email;
    }

    /**
     * 인증 이메일 재전송을 처리합니다.
     * @param email 재전송할 이메일 주소
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("이메일 인증 재전송 요청: {}", maskEmail(email));
        
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 이미 인증된 사용자인지 확인
        if (user.getEmailVerified()) {
            log.warn("이미 인증된 이메일로 재전송 요청: {}", maskEmail(email));
            throw EmailVerificationExceptionUtils.alreadyVerified(email);
        }
        
        // 재전송 제한 확인
        validateResendLimits(email);
        
        // 재전송 요청 기록
        tokenService.recordResendRequest(email);
        
        // 새 토큰 생성 및 이메일 전송 (이전 토큰은 자동으로 무효화됨)
        String token = tokenService.createToken(email);
        emailService.sendVerificationEmail(email, token);
        
        log.info("이메일 인증 재전송 완료: {}", maskEmail(email));
    }

    /**
     * 재전송 제한을 검증합니다.
     * @param email 확인할 이메일 주소
     */
    private void validateResendLimits(String email) {
        // 일일 재전송 횟수 확인
        int dailyCount = tokenService.getDailyResendCount(email);
        if (dailyCount >= DAILY_RESEND_LIMIT) {
            log.warn("일일 재전송 횟수 초과: {} ({}회)", maskEmail(email), dailyCount);
            throw EmailVerificationExceptionUtils.resendLimitExceeded(email, dailyCount, DAILY_RESEND_LIMIT);
        }
        
        // 마지막 재전송 시간 확인 (1분 간격 제한)
        long lastResendTime = tokenService.getLastResendTime(email);
        if (lastResendTime > 0) {
            long currentTime = System.currentTimeMillis() / 1000;
            long timeDiff = currentTime - lastResendTime;
            if (timeDiff < RESEND_INTERVAL_MINUTES * 60) {
                long remainingSeconds = (RESEND_INTERVAL_MINUTES * 60) - timeDiff;
                log.warn("재전송 간격 제한: {} ({}초 후 재시도 가능)", maskEmail(email), remainingSeconds);
                throw EmailVerificationExceptionUtils.resendTooFrequent(email, remainingSeconds);
            }
        }
    }

    /**
     * 이메일 주소를 마스킹합니다.
     * @param email 마스킹할 이메일 주소
     * @return 마스킹된 이메일 주소
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) {
            return "**@" + parts[1];
        }
        return parts[0].substring(0, 2) + "****@" + parts[1];
    }

    /**
     * 인증번호를 마스킹합니다.
     * @param code 마스킹할 인증번호
     * @return 마스킹된 인증번호
     */
    private String maskCode(String code) {
        if (code == null || code.length() != 6) {
            return "****";
        }
        return code.substring(0, 2) + "****";
    }
}