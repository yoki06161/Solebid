package com.sesac.solbid.dto.user.response;

import com.sesac.solbid.domain.User;

import java.math.BigDecimal;

/**
 * 프로필 업데이트 응답 DTO
 * <p>
 * 프로필 업데이트 성공 시 반환되는 사용자 정보를 담는 DTO입니다.
 * </p>
 * 
 * @param userId 사용자 고유 ID
 * @param email 사용자 이메일 주소
 * @param nickname 사용자 닉네임
 * @param name 사용자 이름
 * @param phone 사용자 전화번호
 * @param userType 사용자 타입
 * @param temperature 사용자 온도
 */
public record ProfileUpdateResponse(
    Long userId,
    String email,
    String nickname,
    String name,
    String phone,
    String userType,
    BigDecimal temperature
) {
    /**
     * User 엔티티로부터 ProfileUpdateResponse를 생성합니다.
     * 
     * @param user 사용자 엔티티
     * @return 생성된 ProfileUpdateResponse 객체
     */
    public static ProfileUpdateResponse from(User user) {
        return new ProfileUpdateResponse(
            user.getUserId(),
            user.getEmail(),
            user.getNickname(),
            user.getName(),
            user.getPhone(),
            user.getUserType() != null ? user.getUserType().name() : null,
            user.getTemperature()
        );
    }
}