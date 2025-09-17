package com.sesac.solbid.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인메모리 기반 이메일 인증 토큰 서비스 구현
 * Redis가 없는 환경에서 사용되는 fallback 구현체입니다.
 */
@Slf4j
@Service
@Profile("!redis")
public class InMemoryEmailVerificationTokenService implements EmailVerificationTokenService {

    private static final long TOKEN_TTL_SECONDS = 5 * 60; // 5분
    private static final long RESEND_INTERVAL_SECONDS = 1 * 60; // 1분
    private static final int MAX_DAILY_RESEND = 5; // 일일 최대 재전송 횟수

    private final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();
    private final Map<String, String> emailToTokenStore = new ConcurrentHashMap<>(); // 이메일 -> 토큰 매핑
    private final Map<String, ResendEntry> resendStore = new ConcurrentHashMap<>();
    private final Map<String, DailyCountEntry> dailyCountStore = new ConcurrentHashMap<>();

    @Override
    public String createToken(String email) {
        cleanup();
        
        // 6자리 숫자 인증번호 생성
        String verificationCode = generateVerificationCode();
        Instant expiry = Instant.now().plusSeconds(TOKEN_TTL_SECONDS);
        
        tokenStore.put(verificationCode, new TokenEntry(email, expiry));
        emailToTokenStore.put(email, verificationCode); // 이메일 -> 토큰 매핑 저장
        
        log.debug("[InMemory] 이메일 인증번호 생성: {} for {}", maskToken(verificationCode), maskEmail(email));
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
            log.warn("[InMemory] 토큰 검증 실패: null 또는 빈 토큰");
            return null;
        }
        
        cleanup();
        
        TokenEntry entry = tokenStore.get(token);
        if (entry == null) {
            log.warn("[InMemory] 토큰 검증 실패: 존재하지 않음 - {}, 현재 저장된 토큰 수: {}", 
                    maskToken(token), tokenStore.size());
            // 디버깅을 위해 현재 저장된 토큰들의 마스킹된 정보 출력
            tokenStore.keySet().forEach(key -> 
                log.debug("[InMemory] 저장된 토큰: {}", maskToken(key)));
            return null;
        }
        
        Instant now = Instant.now();
        if (entry.expiry.isBefore(now)) {
            long expiredSeconds = ChronoUnit.SECONDS.between(entry.expiry, now);
            log.warn("[InMemory] 토큰 검증 실패: 만료됨 - {}, {}초 전 만료", 
                    maskToken(token), expiredSeconds);
            tokenStore.remove(token);
            emailToTokenStore.entrySet().removeIf(e -> e.getValue().equals(token));
            return null;
        }
        
        long remainingSeconds = ChronoUnit.SECONDS.between(now, entry.expiry);
        log.info("[InMemory] 토큰 검증 성공: {} for {}, 남은 시간: {}초", 
                maskToken(token), maskEmail(entry.email), remainingSeconds);
        return entry.email;
    }

    @Override
    public String consumeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        cleanup();
        
        TokenEntry entry = tokenStore.remove(token);
        if (entry == null) {
            log.debug("[InMemory] 토큰 소비 실패: 존재하지 않음 - {}", maskToken(token));
            return null;
        }
        
        if (entry.expiry.isBefore(Instant.now())) {
            log.debug("[InMemory] 토큰 소비 실패: 만료됨 - {}", maskToken(token));
            return null;
        }
        
        log.debug("[InMemory] 토큰 소비 성공: {} for {}", maskToken(token), maskEmail(entry.email));
        return entry.email;
    }

    @Override
    public boolean canRequestResend(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        cleanup();
        
        // 1. 일일 재전송 횟수 확인
        int dailyCount = getDailyResendCount(email);
        if (dailyCount >= MAX_DAILY_RESEND) {
            log.debug("[InMemory] 재전송 제한: 일일 한도 초과 ({}/{}) - {}", 
                     dailyCount, MAX_DAILY_RESEND, maskEmail(email));
            return false;
        }
        
        // 2. 마지막 재전송 시간 확인 (1분 간격)
        long lastResendTime = getLastResendTime(email);
        if (lastResendTime > 0) {
            long currentTime = Instant.now().getEpochSecond();
            long timeDiff = currentTime - lastResendTime;
            if (timeDiff < RESEND_INTERVAL_SECONDS) {
                log.debug("[InMemory] 재전송 제한: 시간 간격 부족 ({}초 < {}초) - {}", 
                         timeDiff, RESEND_INTERVAL_SECONDS, maskEmail(email));
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
        
        cleanup();
        
        long currentTime = Instant.now().getEpochSecond();
        
        // 1. 마지막 재전송 시간 기록
        Instant resendExpiry = Instant.now().plusSeconds(RESEND_INTERVAL_SECONDS);
        resendStore.put(email, new ResendEntry(currentTime, resendExpiry));
        
        // 2. 일일 재전송 횟수 증가
        String dailyKey = getDailyKey(email);
        DailyCountEntry dailyEntry = dailyCountStore.get(dailyKey);
        
        if (dailyEntry == null || !dailyEntry.date.equals(LocalDate.now())) {
            // 새로운 날짜이거나 첫 번째 재전송
            dailyCountStore.put(dailyKey, new DailyCountEntry(LocalDate.now(), 1));
            log.debug("[InMemory] 재전송 요청 기록: {} (일일 1회)", maskEmail(email));
        } else {
            // 기존 날짜의 재전송 횟수 증가
            dailyCountStore.put(dailyKey, new DailyCountEntry(dailyEntry.date, dailyEntry.count + 1));
            log.debug("[InMemory] 재전송 요청 기록: {} (일일 {}회)", maskEmail(email), dailyEntry.count + 1);
        }
    }

    @Override
    public int getDailyResendCount(String email) {
        if (email == null || email.trim().isEmpty()) {
            return 0;
        }
        
        cleanup();
        
        String dailyKey = getDailyKey(email);
        DailyCountEntry entry = dailyCountStore.get(dailyKey);
        
        if (entry == null || !entry.date.equals(LocalDate.now())) {
            return 0;
        }
        
        return entry.count;
    }

    @Override
    public long getLastResendTime(String email) {
        if (email == null || email.trim().isEmpty()) {
            return -1;
        }
        
        cleanup();
        
        ResendEntry entry = resendStore.get(email);
        if (entry == null) {
            return -1;
        }
        
        if (entry.expiry.isBefore(Instant.now())) {
            resendStore.remove(email);
            return -1;
        }
        
        return entry.lastResendTime;
    }

    @Override
    public boolean hasValidToken(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        cleanup();
        
        String token = emailToTokenStore.get(email);
        if (token == null) {
            return false;
        }
        
        TokenEntry entry = tokenStore.get(token);
        return entry != null && entry.expiry.isAfter(Instant.now());
    }

    @Override
    public long getRemainingTimeSeconds(String email) {
        if (email == null || email.trim().isEmpty()) {
            return 0;
        }
        
        cleanup();
        
        String token = emailToTokenStore.get(email);
        if (token == null) {
            return 0;
        }
        
        TokenEntry entry = tokenStore.get(token);
        if (entry == null) {
            return 0;
        }
        
        Instant now = Instant.now();
        if (entry.expiry.isBefore(now)) {
            return 0;
        }
        
        return ChronoUnit.SECONDS.between(now, entry.expiry);
    }

    @Override
    public int getRemainingAttempts(String email) {
        if (email == null || email.trim().isEmpty()) {
            return 0;
        }
        
        cleanup();
        
        // 간단한 구현: 유효한 토큰이 있으면 3회 시도 가능으로 가정
        // 실제로는 시도 횟수를 별도로 추적해야 하지만, 현재 구조에서는 간단히 처리
        return hasValidToken(email) ? 3 : 0;
    }

    /**
     * 만료된 엔트리들을 정리합니다.
     */
    private void cleanup() {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now();
        
        // 만료된 토큰 정리
        tokenStore.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().expiry.isBefore(now);
            if (expired) {
                // 이메일 -> 토큰 매핑도 함께 정리
                emailToTokenStore.entrySet().removeIf(emailEntry -> 
                    emailEntry.getValue().equals(entry.getKey()));
            }
            return expired;
        });
        
        // 만료된 재전송 기록 정리
        resendStore.entrySet().removeIf(entry -> entry.getValue().expiry.isBefore(now));
        
        // 오래된 일일 카운트 정리 (7일 이상 된 것)
        dailyCountStore.entrySet().removeIf(entry -> 
            entry.getValue().date.isBefore(today.minusDays(7)));
    }

    /**
     * 일일 키 생성
     */
    private String getDailyKey(String email) {
        return LocalDate.now() + ":" + email;
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

    /**
     * 토큰 엔트리
     */
    private record TokenEntry(String email, Instant expiry) {}

    /**
     * 재전송 엔트리
     */
    private record ResendEntry(long lastResendTime, Instant expiry) {}

    /**
     * 일일 카운트 엔트리
     */
    private record DailyCountEntry(LocalDate date, int count) {}
}