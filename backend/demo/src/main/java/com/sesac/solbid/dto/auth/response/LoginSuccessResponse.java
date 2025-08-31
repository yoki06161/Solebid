package com.sesac.solbid.dto.auth.response;

import lombok.Builder;
import lombok.Getter;

/**
 * OAuth2 로그인 성공 응답 DTO (토큰 제외)
 */
@Getter
public class LoginSuccessResponse {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final String userType;
    private final String provider;
    private final boolean requiresNickname;

    @Builder
    public LoginSuccessResponse(Long userId, String email, String nickname,
                                Object userType, String provider, boolean requiresNickname) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.userType = userType != null ? userType.toString() : null;
        this.provider = provider;
        this.requiresNickname = requiresNickname;
    }
}

