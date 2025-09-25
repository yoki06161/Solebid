package com.sesac.solbid.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${portone.base-url}")
    private String portoneBaseUrl;

    /*
    * portOne 설정*/
    @Bean
    public WebClient portOneWebClient() {
        return WebClient.builder()
                .baseUrl(portoneBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


}
