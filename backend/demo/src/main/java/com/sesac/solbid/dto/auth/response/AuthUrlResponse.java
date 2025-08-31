package com.sesac.solbid.dto.auth.response;

import lombok.Builder;
import lombok.Getter;

/**
 * OAuth2 인증 URL 응답 DTO
 */
@Getter
public class AuthUrlResponse {
    private final String authUrl;
    private final String state;
    private final String provider;

    @Builder
    public AuthUrlResponse(String authUrl, String state, String provider) {
        this.authUrl = authUrl;
        this.state = state;
        this.provider = provider;
    }

    public static AuthUrlResponse of(String authUrl, String state, String provider) {
        return AuthUrlResponse.builder()
                .authUrl(authUrl)
                .state(state)
                .provider(provider)
                .build();
    }
}

