package com.sesac.solbid.dto.user.response;

import com.sesac.solbid.domain.User;
import lombok.Getter;

@Getter
public class SignupResponse {
    private final Long userId;
    private final String email;
    private final String nickname;

    public SignupResponse(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
    }
}

