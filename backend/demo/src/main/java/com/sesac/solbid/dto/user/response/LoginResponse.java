package com.sesac.solbid.dto.user.response;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.UserType;

/**
 * 사용자 로그인 응답 DTO
 * <p>
 * 사용자 로그인 성공 시 반환되는 정보를 담는 DTO입니다.
 * JWT 토큰과 사용자 기본 정보를 포함합니다.
 * </p>
 * 
 * @param userId 사용자 고유 ID
 * @param email 사용자 이메일 주소
 * @param nickname 사용자 닉네임
 * @param name 사용자 이름
 * @param phone 사용자 전화번호
 * @param userType 사용자 타입 (USER, ADMIN 등)
 * @param accessToken JWT 액세스 토큰
 * @param refreshToken JWT 리프레시 토큰
 */
public record LoginResponse(
    Long userId,
    String email,
    String nickname,
    String name,
    String phone,
    UserType userType,
    String accessToken,
    String refreshToken
) {
    /**
     * User 엔티티와 토큰 정보로부터 LoginResponse를 생성합니다.
     * 
     * @param user 사용자 엔티티
     * @param accessToken JWT 액세스 토큰
     * @param refreshToken JWT 리프레시 토큰
     * @return 생성된 LoginResponse 객체
     */
    public static LoginResponse from(User user, String accessToken, String refreshToken) {
        return new LoginResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getPhone(),
                user.getUserType(),
                accessToken,
                refreshToken
        );
    }
}

