package com.sesac.solbid.integration;

import com.sesac.solbid.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

final class TestJwtFactory {
    private TestJwtFactory() {}

    static String generateExpiredAccessToken(JwtUtil jwtUtil, String subject) {
        Key key = extractKey(jwtUtil);
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now - 3600_000); // 1시간 전 발급
        Date expiredAt = new Date(now - 1_000);   // 이미 만료
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private static Key extractKey(JwtUtil jwtUtil) {
        try {
            Field keyField = JwtUtil.class.getDeclaredField("key");
            keyField.setAccessible(true);
            Object keyObj = keyField.get(jwtUtil);
            if (keyObj instanceof Key) {
                return (Key) keyObj;
            }
            // fallback: secret 문자열에서 키 재구성
            Field secretField = JwtUtil.class.getDeclaredField("secret");
            secretField.setAccessible(true);
            String secret = (String) secretField.get(jwtUtil);
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract signing key from JwtUtil", e);
        }
    }
}

