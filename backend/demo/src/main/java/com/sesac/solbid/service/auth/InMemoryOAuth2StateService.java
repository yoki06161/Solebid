package com.sesac.solbid.service.auth;

import com.sesac.solbid.exception.OAuth2StateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 기본 인메모리 OAuth2 State 서비스 구현
 * 프로필: !redis (테스트 및 로컬 기본)
 */
@Slf4j
@Service
@Profile("!redis")
public class InMemoryOAuth2StateService implements OAuth2StateService {

    private static final int STATE_TTL_MINUTES = 15;

    private final Map<String, StateInfo> stateStore = new ConcurrentHashMap<>();

    @Override
    public String generateState() {
        String state = UUID.randomUUID().toString();
        StateInfo stateInfo = new StateInfo(LocalDateTime.now().plusMinutes(STATE_TTL_MINUTES));
        stateStore.put(state, stateInfo);
        log.debug("OAuth2 state 생성: {}", maskState(state));
        return state;
    }

    @Override
    public boolean validateState(String state) {
        if (state == null || state.trim().isEmpty()) {
            log.warn("OAuth2 state 검증 실패: state가 null 또는 빈 값 - 잠재적 CSRF 공격 시도");
            throw new OAuth2StateException();
        }
        StateInfo stateInfo = stateStore.get(state);
        if (stateInfo == null) {
            log.warn("OAuth2 state 검증 실패: 존재하지 않는 state - {} - 잠재적 CSRF 공격 시도", maskState(state));
            throw new OAuth2StateException();
        }
        if (stateInfo.isExpired()) {
            log.warn("OAuth2 state 검증 실패: 만료된 state - {} - 만료시간: {}",
                    maskState(state), stateInfo.getExpiryTime());
            stateStore.remove(state);
            throw new OAuth2StateException();
        }
        log.debug("OAuth2 state 검증 성공: {} - 생성시간: {}",
                maskState(state), stateInfo.getCreatedTime());
        return true;
    }

    @Override
    public void consumeState(String state) {
        if (state == null || state.trim().isEmpty()) {
            log.warn("OAuth2 state 소비 실패: state가 null 또는 빈 값");
            throw new OAuth2StateException();
        }
        StateInfo stateInfo = stateStore.remove(state);
        if (stateInfo == null) {
            log.warn("OAuth2 state 소비 실패: 존재하지 않거나 이미 소비된 state - {}", maskState(state));
            throw new OAuth2StateException();
        }
        if (stateInfo.isExpired()) {
            log.warn("OAuth2 state 소비 실패: 만료된 state - {} - 만료시간: {}", maskState(state), stateInfo.getExpiryTime());
            throw new OAuth2StateException();
        }
        log.debug("OAuth2 state 소비 성공: {}", maskState(state));
    }

    @Override
    public void removeState(String state) {
        if (state != null) {
            stateStore.remove(state);
            log.debug("OAuth2 state 삭제: {}", maskState(state));
        }
    }

    @Scheduled(fixedRate = 600000)
    public void cleanupExpiredStates() {
        AtomicInteger removedCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(stateStore.size());
        stateStore.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                removedCount.incrementAndGet();
                return true;
            }
            return false;
        });
        int removed = removedCount.get();
        int remaining = stateStore.size();
        if (removed > 0) {
            log.info("OAuth2 state 정리 완료 - 만료: {}, 유지: {}, 전체: {}",
                    removed, remaining, totalCount.get());
        }
        if (remaining > 1000) {
            log.warn("OAuth2 state 개수가 비정상적으로 많음: {} - 잠재적 공격 가능성 검토 필요", remaining);
        }
    }

    @Override
    public int getStateCount() {
        return stateStore.size();
    }

    private String maskState(String state) {
        if (state == null || state.length() < 8) {
            return "****";
        }
        return state.substring(0, 4) + "****" + state.substring(state.length() - 4);
    }

    private static class StateInfo {
        private final LocalDateTime createdTime;
        private final LocalDateTime expiryTime;

        public StateInfo(LocalDateTime expiryTime) {
            this.createdTime = LocalDateTime.now();
            this.expiryTime = expiryTime;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }

        public LocalDateTime getCreatedTime() {
            return createdTime;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
}

