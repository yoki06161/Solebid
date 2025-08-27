package com.sesac.solbid.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Getter
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-seconds}")
    private long accessTokenValiditySeconds;

    @Value("${jwt.refresh-token-validity-seconds}")
    private long refreshTokenValiditySeconds;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(String username) {
        return doGenerateToken(username, accessTokenValiditySeconds * 1000);
    }

    public String generateRefreshToken(String username) {
        return doGenerateToken(username, refreshTokenValiditySeconds * 1000);
    }

    private String doGenerateToken(String subject, long validityInMilliseconds) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validityInMilliseconds))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }


    // 토큰만으로 서명 및 만료 여부를 검증하는 보조 메서드
    public boolean validateToken(String token) {
        try {
            // 서명 검증 및 클레임 파싱
            getAllClaimsFromToken(token);
            // 만료 여부 확인
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}