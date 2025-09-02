package com.sesac.solbid.dto.payment.request;


import com.fasterxml.jackson.annotation.JsonProperty;

public class PortOneTokenRequest {
    @JsonProperty("imp_key")
    private String apiKey;

    @JsonProperty("imp_secret")
    private String apiSecret;

    public PortOneTokenRequest(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }
}
