package com.sesac.solbid.service.user;

import com.sesac.solbid.domain.User;

import com.sesac.solbid.exception.PasswordResetExceptionUtils;
import com.sesac.solbid.repository.auth.SocialLoginRepository;
import com.sesac.solbid.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenService tokenService;
    private final EmailService emailService;

    // 재전송 제한 상수
    private static final int RESEND_INTERVAL_MINUTES = 1;
    private static final int DAILY_RESEND_LIMIT = 5;

    /**
     * 비밀번호 재설정 OTP 요청
     * @param email 비밀번호를 재설정할 이메일 주소
     */
    @Transactional
    public void requestResetWithOtp(String email) {
        log.info("비밀번호 재설정 OTP 요청: {}", maskEmail(email));
        
        // 보안상 존재하지 않는 이메일에 대해서도 동일한 응답을 제공
        // 하지만 실제로는 존재하는 사용자에게만 이메일을 발송
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null) {
            // 소셜 로그인 사용자이면서 비밀번호가 없는 경우 비밀번호 재설정 불가
            if ((user.getPassword() == null || user.getPassword().isBlank()) && 
                socialLoginRepository.findByUser(user).isPresent()) {
                throw PasswordResetExceptionUtils.resetNotAllowed(email);
            }
            
            // OTP 생성 및 이메일 발송
            String otp = tokenService.createToken(email);
            emailService.sendPasswordResetOtp(email, otp);
            
            log.info("비밀번호 재설정 OTP 발송 완료: {}", maskEmail(email));
        } else {
            log.info("존재하지 않는 이메일로 비밀번호 재설정 요청: {}", maskEmail(email));
            // 보안상 동일한 응답을 제공하지만 실제로는 이메일을 발송하지 않음
        }
    }

    /**
     * OTP 검증만 수행 (비밀번호 재설정 없이)
     * @param email 이메일 주소
     * @param otp 6자리 인증번호
     * @return 검증된 이메일 주소
     */
    @Transactional(readOnly = true)
    public String verifyOtpOnly(String email, String otp) {
        log.info("비밀번호 재설정 OTP 검증: email={}, otp={}", maskEmail(email), maskOtp(otp));
        
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> PasswordResetExceptionUtils.userNotFound(email));
        
        // 토큰 유효성 확인을 위한 추가 로그
        boolean hasValidToken = tokenService.hasValidToken(email);
        long remainingTime = tokenService.getRemainingTimeSeconds(email);
        log.debug("토큰 상태 확인: email={}, hasValidToken={}, remainingTimeSeconds={}", 
                maskEmail(email), hasValidToken, remainingTime);
        
        // OTP 검증 및 토큰에서 이메일 확인
        String tokenEmail = tokenService.getEmailIfValid(otp);
        if (tokenEmail == null) {
            log.warn("유효하지 않은 비밀번호 재설정 OTP: email={}, otp={}, tokenEmail=null, hasValidToken={}, remainingTime={}", 
                    maskEmail(email), maskOtp(otp), hasValidToken, remainingTime);
            throw PasswordResetExceptionUtils.invalidOtp(email);
        }
        
        if (!tokenEmail.equals(email)) {
            log.warn("비밀번호 재설정 OTP 이메일 불일치: email={}, tokenEmail={}, otp={}", 
                    maskEmail(email), maskEmail(tokenEmail), maskOtp(otp));
            throw PasswordResetExceptionUtils.invalidOtp(email);
        }
        
        log.info("비밀번호 재설정 OTP 검증 성공: {}", maskEmail(email));
        return email;
    }

    /**
     * OTP 검증 및 비밀번호 재설정
     * @param email 이메일 주소
     * @param otp 6자리 인증번호
     * @param newPassword 새로운 비밀번호
     */
    @Transactional
    public void verifyOtpAndReset(String email, String otp, String newPassword) {
        log.info("비밀번호 재설정 OTP 검증 및 재설정: email={}, otp={}", maskEmail(email), maskOtp(otp));
        
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> PasswordResetExceptionUtils.userNotFound(email));
        
        // OTP 검증 및 소비
        String tokenEmail = tokenService.consumeToken(otp);
        if (tokenEmail == null || !tokenEmail.equals(email)) {
            log.warn("유효하지 않은 비밀번호 재설정 OTP: email={}, otp={}", maskEmail(email), maskOtp(otp));
            throw PasswordResetExceptionUtils.invalidOtp(email);
        }
        
        // 기존 비밀번호와 동일한지 확인
        if (user.getPassword() != null && passwordEncoder.matches(newPassword, user.getPassword())) {
            throw PasswordResetExceptionUtils.sameAsOldPassword(email);
        }
        
        // 비밀번호 업데이트
        user.updatePassword(passwordEncoder.encode(newPassword));
        
        log.info("비밀번호 재설정 완료: {}", maskEmail(email));
    }

    /**
     * 비밀번호 재설정 OTP 재전송
     * @param email 재전송할 이메일 주소
     */
    @Transactional
    public void resendResetOtp(String email) {
        log.info("비밀번호 재설정 OTP 재전송 요청: {}", maskEmail(email));
        
        // 사용자 존재 여부 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> PasswordResetExceptionUtils.userNotFound(email));
        
        // 소셜 로그인 사용자이면서 비밀번호가 없는 경우 비밀번호 재설정 불가
        if ((user.getPassword() == null || user.getPassword().isBlank()) && 
            socialLoginRepository.findByUser(user).isPresent()) {
            throw PasswordResetExceptionUtils.resetNotAllowed(email);
        }
        
        // 재전송 제한 확인
        validateResendLimits(email);
        
        // 재전송 요청 기록
        tokenService.recordResendRequest(email);
        
        // 새 OTP 생성 및 이메일 전송 (이전 OTP는 자동으로 무효화됨)
        String otp = tokenService.createToken(email);
        emailService.sendPasswordResetOtp(email, otp);
        
        log.info("비밀번호 재설정 OTP 재전송 완료: {}", maskEmail(email));
    }



    /**
     * 재전송 제한을 검증합니다.
     * @param email 확인할 이메일 주소
     */
    private void validateResendLimits(String email) {
        // EmailVerificationTokenService의 canRequestResend 메서드를 사용하여 재전송 가능 여부 확인
        if (!tokenService.canRequestResend(email)) {
            // 일일 재전송 횟수 확인
            int dailyCount = tokenService.getDailyResendCount(email);
            if (dailyCount >= DAILY_RESEND_LIMIT) {
                log.warn("일일 재전송 횟수 초과: {} ({}회)", maskEmail(email), dailyCount);
                throw PasswordResetExceptionUtils.resendLimitExceeded(email, dailyCount, DAILY_RESEND_LIMIT);
            }
            
            // 마지막 재전송 시간 확인 (1분 간격 제한)
            long lastResendTime = tokenService.getLastResendTime(email);
            if (lastResendTime > 0) {
                long currentTime = System.currentTimeMillis() / 1000;
                long timeDiff = currentTime - lastResendTime;
                if (timeDiff < RESEND_INTERVAL_MINUTES * 60) {
                    long remainingSeconds = (RESEND_INTERVAL_MINUTES * 60) - timeDiff;
                    log.warn("재전송 간격 제한: {} ({}초 후 재시도 가능)", maskEmail(email), remainingSeconds);
                    throw PasswordResetExceptionUtils.resendTooFrequent(email, remainingSeconds);
                }
            }
            
            // 기타 재전송 제한 (일반적인 경우)
            log.warn("재전송 제한: {}", maskEmail(email));
            throw PasswordResetExceptionUtils.resendLimitExceeded(email, dailyCount, DAILY_RESEND_LIMIT);
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
     * OTP를 마스킹합니다.
     * @param otp 마스킹할 OTP
     * @return 마스킹된 OTP
     */
    private String maskOtp(String otp) {
        if (otp == null || otp.length() != 6) {
            return "****";
        }
        return otp.substring(0, 2) + "****";
    }
}
