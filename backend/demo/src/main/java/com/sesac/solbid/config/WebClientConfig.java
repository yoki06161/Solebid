package com.sesac.solbid.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebClientConfig implements WebMvcConfigurer {

    @Value("${portone.base-url}")
    private String portoneBaseUrl;

    @Bean
    public WebClient portOneWebClient() {
        return WebClient.builder()
                .baseUrl(portoneBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")       // 개발 중엔 전체 허용
                .allowedMethods("*")       // GET, POST 등 허용
                .allowedHeaders("*")       // 모든 헤더 허용
                .allowCredentials(false);  // 필요 시 true로 설정
    }
}
