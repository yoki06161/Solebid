package com.sesac.solbid.service.auth;

import com.sesac.solbid.exception.OAuth2StateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 OAuth2 State 서비스 구현
 * 프로필: redis
 */
@Slf4j
@Service
@Profile("redis")
@RequiredArgsConstructor
public class RedisOAuth2StateService implements OAuth2StateService {

    private static final Duration STATE_TTL = Duration.ofMinutes(15);
    private static final String KEY_PREFIX = "oauth2:state:";

    private final StringRedisTemplate redis;

    @Override
    public String generateState() {
        String state = UUID.randomUUID().toString();
        String key = key(state);
        // 값은 생성시각 등 의미 없는 placeholder여도 됨
        redis.opsForValue().set(key, "1", STATE_TTL);
        log.debug("[Redis] OAuth2 state 생성: {}", maskState(state));
        return state;
    }

    @Override
    public boolean validateState(String state) {
        requireState(state);
        String key = key(state);
        Boolean exists = redis.hasKey(key);
        if (exists == null || !exists) {
            log.warn("[Redis] OAuth2 state 검증 실패: 존재하지 않음 - {}", maskState(state));
            throw new OAuth2StateException();
        }
        Long ttl = redis.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl <= 0) { // -2: 없음, 0 이하: 만료 임박/이상치도 실패 처리
            log.warn("[Redis] OAuth2 state 검증 실패: 만료됨 - {}", maskState(state));
            redis.delete(key); // 청소
            throw new OAuth2StateException();
        }
        log.debug("[Redis] OAuth2 state 검증 성공: {} (ttl={}s)", maskState(state), ttl);
        return true;
    }

    @Override
    public void consumeState(String state) {
        requireState(state);
        String key = key(state);
        Long ttl = redis.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl <= 0) {
            log.warn("[Redis] OAuth2 state 소비 실패: 만료 또는 미존재 - {}", maskState(state));
            throw new OAuth2StateException();
        }
        Boolean removed = redis.delete(key);
        if (removed == null || !removed) {
            log.warn("[Redis] OAuth2 state 소비 실패: 이미 소비 되었거나 존재하지 않음 - {}", maskState(state));
            throw new OAuth2StateException();
        }
        log.debug("[Redis] OAuth2 state 소비 성공: {}", maskState(state));
    }

    @Override
    public void removeState(String state) {
        if (state == null) return;
        redis.delete(key(state));
        log.debug("[Redis] OAuth2 state 삭제: {}", maskState(state));
    }

    @Override
    public int getStateCount() {
        // 운영 환경에서는 키 스캔 비용이 있을 수 있음. 모니터링용으로만 사용.
        Set<String> keys = redis.keys(KEY_PREFIX + "*");
        return keys == null ? 0 : keys.size();
    }

    private String key(String state) {
        return KEY_PREFIX + state;
    }

    private void requireState(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new OAuth2StateException();
        }
    }

    private String maskState(String state) {
        if (state == null || state.length() < 8) return "****";
        return state.substring(0, 4) + "****" + state.substring(state.length() - 4);
    }
}

