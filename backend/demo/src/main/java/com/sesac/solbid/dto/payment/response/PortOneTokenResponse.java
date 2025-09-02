package com.sesac.solbid.dto.payment.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortOneTokenResponse {
    private TokenData response;

    @Getter
    @Setter
    public static class TokenData {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expired_at")
        private Long expiredAt;
    }
}


