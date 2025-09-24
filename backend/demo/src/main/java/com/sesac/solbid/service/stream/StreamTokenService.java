package com.sesac.solbid.service.stream;

public interface StreamTokenService {
    String issue(Long userId);                 // 토큰 발급 (TTL 포함)
    Long validate(String token);               // 검증 + TTL 갱신(슬라이딩)
    void revoke(String token);                 // 강제 만료
}
