package com.sesac.solbid.dto.user.response;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.UserType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponse {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final UserType userType;
    private final String accessToken;
    private final String refreshToken;

    @Builder
    public LoginResponse(Long userId, String email, String nickname, UserType userType, String accessToken, String refreshToken) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.userType = userType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static LoginResponse from(User user, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userType(user.getUserType())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}

