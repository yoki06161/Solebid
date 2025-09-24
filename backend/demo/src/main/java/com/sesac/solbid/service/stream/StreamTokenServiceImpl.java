package com.sesac.solbid.service.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StreamTokenServiceImpl implements StreamTokenService {

    private final StringRedisTemplate redis;
    private static final Duration TTL = Duration.ofMinutes(5);

    /**Redis 키 프리픽스(ssetok:)를 붙여 토큰 키 생성*/
    private String key(String token) { return "ssetok:" + token; }

    /**사용자 ID 기반 랜덤 UUID 토큰을 발급, Redis에 5분 TTL로 저장*/
    @Override
    public String issue(Long userId) {
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(key(token), String.valueOf(userId), TTL);
        return token;
    }

    /**토큰을 검증 사용자 ID를 반환, 유효하면 TTL을 연장(슬라이딩 만료)*/
    @Override
    public Long validate(String token) {
        if (token == null || token.isBlank()) return null;
        String k = key(token);
        String userId = redis.opsForValue().get(k);
        if (userId == null) return null; // 만료/무효

        //  슬라이딩 TTL : 검증 성공 시 TTL 연장
        redis.expire(k, TTL);
        return Long.valueOf(userId);
    }

    /**토큰을 Redis에서 삭제 후 무효화*/
    @Override
    public void revoke(String token) {
        redis.delete(key(token));
    }
}
