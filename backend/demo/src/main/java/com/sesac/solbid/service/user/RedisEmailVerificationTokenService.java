package com.sesac.solbid.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 이메일 인증 토큰 서비스 구현
 * 토큰 생성, 검증, 소비, 만료 처리 및 재전송 제한 기능을 제공합니다.
 */
@Slf4j
@Service
@Profile("redis")
@RequiredArgsConstructor
public class RedisEmailVerificationTokenService implements EmailVerificationTokenService {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(5); // 5분 토큰 유효기간
    private static final Duration RESEND_INTERVAL = Duration.ofMinutes(1); // 1분 재전송 간격
    private static final int MAX_DAILY_RESEND = 5; // 일일 최대 재전송 횟수
    
    private static final String TOKEN_KEY_PREFIX = "email_verification:code:";
    private static final String RESEND_KEY_PREFIX = "email_verification:resend:";
    private static final String DAILY_COUNT_KEY_PREFIX = "email_verification:daily:";

    private final StringRedisTemplate redis;

    @Override
    public String createToken(String email) {
        // 6자리 숫자 인증번호 생성
        String verificationCode = generateVerificationCode();
        String key = tokenKey(verificationCode);
        
        // 인증번호와 이메일을 Redis에 저장 (5분 TTL)
        redis.opsForValue().set(key, email, TOKEN_TTL);
        
        // 이메일로 토큰을 역조회할 수 있도록 저장 (OTP 상태 조회용)
        String emailTokenKey = "email_verification:email:" + email;
        redis.opsForValue().set(emailTokenKey, verificationCode, TOKEN_TTL);
        
        log.debug("[Redis] 이메일 인증번호 생성: {} for {}", maskToken(verificationCode), maskEmail(email));
        return verificationCode;
    }
    
    /**
     * 6자리 숫자 인증번호 생성
     */
    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    @Override
    public String getEmailIfValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        String key = tokenKey(token);
        String email = redis.opsForValue().get(key);
        
        if (email == null) {
            log.debug("[Redis] 토큰 검증 실패: 존재하지 않거나 만료됨 - {}", maskToken(token));
            return null;
        }
        
        log.debug("[Redis] 토큰 검증 성공: {} for {}", maskToken(token), maskEmail(email));
        return email;
    }

    @Override
    public String consumeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        String key = tokenKey(token);
        String email = redis.opsForValue().get(key);
        
        if (email == null) {
            log.debug("[Redis] 토큰 소비 실패: 존재하지 않거나 만료됨 - {}", maskToken(token));
            return null;
        }
        
        // 토큰 삭제 (일회성 사용)
        Boolean deleted = redis.delete(key);
        if (deleted == null || !deleted) {
            log.warn("[Redis] 토큰 삭제 실패: {}", maskToken(token));
            return null;
        }
        
        log.debug("[Redis] 토큰 소비 성공: {} for {}", maskToken(token), maskEmail(email));
        return email;
    }

    @Override
    public boolean canRequestResend(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // 1. 일일 재전송 횟수 확인
        int dailyCount = getDailyResendCount(email);
        if (dailyCount >= MAX_DAILY_RESEND) {
            log.debug("[Redis] 재전송 제한: 일일 한도 초과 ({}/{}) - {}", 
                     dailyCount, MAX_DAILY_RESEND, maskEmail(email));
            return false;
        }
        
        // 2. 마지막 재전송 시간 확인 (1분 간격)
        long lastResendTime = getLastResendTime(email);
        if (lastResendTime > 0) {
            long currentTime = Instant.now().getEpochSecond();
            long timeDiff = currentTime - lastResendTime;
            if (timeDiff < RESEND_INTERVAL.getSeconds()) {
                log.debug("[Redis] 재전송 제한: 시간 간격 부족 ({}초 < {}초) - {}", 
                         timeDiff, RESEND_INTERVAL.getSeconds(), maskEmail(email));
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void recordResendRequest(String email) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }
        
        long currentTime = Instant.now().getEpochSecond();
        
        // 1. 마지막 재전송 시간 기록 (1분 TTL)
        String resendKey = resendKey(email);
        redis.opsForValue().set(resendKey, String.valueOf(currentTime), RESEND_INTERVAL);
        
        // 2. 일일 재전송 횟수 증가 (자정까지 TTL)
        String dailyKey = dailyCountKey(email);
        Long count = redis.opsForValue().increment(dailyKey);
        
        // 첫 번째 재전송인 경우 TTL 설정 (자정까지)
        if (count != null && count == 1) {
            long secondsUntilMidnight = getSecondsUntilMidnight();
            redis.expire(dailyKey, secondsUntilMidnight, TimeUnit.SECONDS);
        }
        
        log.debug("[Redis] 재전송 요청 기록: {} (일일 {}회)", maskEmail(email), count);
    }

    @Override
    public int getDailyResendCount(String email) {
        if (email == null || email.trim().isEmpty()) {
            return 0;
        }
        
        String dailyKey = dailyCountKey(email);
        String countStr = redis.opsForValue().get(dailyKey);
        
        if (countStr == null) {
            return 0;
        }
        
        try {
            return Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            log.warn("[Redis] 일일 재전송 횟수 파싱 실패: {} for {}", countStr, maskEmail(email));
            return 0;
        }
    }

    @Override
    public long getLastResendTime(String email) {
        if (email == null || email.trim().isEmpty()) {
            return -1;
        }
        
        String resendKey = resendKey(email);
        String timeStr = redis.opsForValue().get(resendKey);
        
        if (timeStr == null) {
            return -1;
        }
        
        try {
            return Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            log.warn("[Redis] 마지막 재전송 시간 파싱 실패: {} for {}", timeStr, maskEmail(email));
            return -1;
        }
    }

    @Override
    public boolean hasValidToken(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Redis에서 해당 이메일로 생성된 유효한 토큰이 있는지 확인
        // 실제로는 이메일로 토큰을 역조회하는 것은 비효율적이므로
        // 별도의 키를 사용하여 추적하는 것이 좋지만, 현재 구조에서는 간단히 처리
        String emailTokenKey = "email_verification:email:" + email;
        String token = redis.opsForValue().get(emailTokenKey);
        
        if (token == null) {
            return false;
        }
        
        // 해당 토큰이 실제로 유효한지 확인
        String tokenKey = tokenKey(token);
        String storedEmail = redis.opsForValue().get(tokenKey);
        return email.equals(storedEmail);
    }

    @Override
    public long getRemainingTimeSeconds(String email) {
        if (email == null || email.trim().isEmpty()) {
            return 0;
        }
        
        String emailTokenKey = "email_verification:email:" + email;
        String token = redis.opsForValue().get(emailTokenKey);
        
        if (token == null) {
            return 0;
        }
        
        String tokenKey = tokenKey(token);
        Long ttl = redis.getExpire(tokenKey, TimeUnit.SECONDS);
        
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    @Override
    public int getRemainingAttempts(String email) {
        if (email == null || email.trim().isEmpty()) {
            return 0;
        }
        
        // 간단한 구현: 유효한 토큰이 있으면 3회 시도 가능으로 가정
        // 실제로는 시도 횟수를 별도로 추적해야 하지만, 현재 구조에서는 간단히 처리
        return hasValidToken(email) ? 3 : 0;
    }

    /**
     * 토큰 키 생성
     */
    private String tokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }

    /**
     * 재전송 키 생성
     */
    private String resendKey(String email) {
        return RESEND_KEY_PREFIX + email;
    }

    /**
     * 일일 카운트 키 생성
     */
    private String dailyCountKey(String email) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return DAILY_COUNT_KEY_PREFIX + today + ":" + email;
    }

    /**
     * 자정까지 남은 초 계산
     */
    private long getSecondsUntilMidnight() {
        Instant now = Instant.now();
        Instant midnight = now.truncatedTo(java.time.temporal.ChronoUnit.DAYS)
                             .plus(Duration.ofDays(1));
        return Duration.between(now, midnight).getSeconds();
    }

    /**
     * 토큰 마스킹 (보안을 위해)
     */
    private String maskToken(String token) {
        if (token == null || token.length() != 6) {
            return "****";
        }
        return token.substring(0, 2) + "****";
    }

    /**
     * 이메일 마스킹 (보안을 위해)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return "**@" + domain;
        }
        
        return localPart.substring(0, 2) + "****@" + domain;
    }
}