package com.sesac.solbid.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 헬스체크 엔드포인트는 인증 없이 접근 허용 (Docker 헬스체크용) - 최우선 순위
                        .requestMatchers(
                                "/actuator/**"
                        ).permitAll()
                        // 회원가입, 일반 로그인 API는 인증 없이 접근 허용
                        .requestMatchers(
                                "/api/users/signup",
                                "/api/users/login"
                        ).permitAll()
                        // 이메일 인증 관련 API는 인증 없이 접근 허용
                        .requestMatchers(
                                "/api/auth/verify-email",
                                "/api/auth/verify-code",
                                "/api/auth/verify-signup-code",
                                "/api/auth/send-verification",
                                "/api/auth/resend-verification"
                        ).permitAll()
                        // OAuth2 소셜로그인 및 로그아웃/리프레시/상태 API는 인증 없이 접근 허용
                        .requestMatchers(
                                "/api/auth/oauth2/*/url",
                                "/api/auth/oauth2/*/callback",
                                "/api/auth/logout",
                                "/api/auth/refresh",
                                "/api/auth/status"
                        ).permitAll()
                        // 비밀번호 재설정 OTP 관련 API는 인증 없이 접근 허용
                        .requestMatchers(
                                "/api/auth/password/request-reset",
                                "/api/auth/password/verify-otp",
                                "/api/auth/password/verify-and-reset",
                                "/api/auth/password/resend-otp"
                        ).permitAll()
                        .requestMatchers(
                                "/api/products"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET, "/api/products/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auctions/**",
                                "/api/auction-events/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST, "/api/uploads/download-urls"
                        ).permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 인증 필터 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 오리진 설정 (HTTP와 HTTPS 모두 지원)
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",           // HTTP 로컬호스트
                "https://localhost:*",          // HTTPS 로컬호스트
                "http://127.0.0.1:*",           // HTTP 127.0.0.1
                "https://127.0.0.1:*",          // HTTPS 127.0.0.1
                "http://frontend:*",            // Docker 네트워크 내부 통신 (HTTP)
                "https://frontend:*",           // Docker 네트워크 내부 통신 (HTTPS)
                "http://solebid-frontend:*",   // Docker 컨테이너명 기반 통신 (HTTP)
                "https://solebid-frontend:*"   // Docker 컨테이너명 기반 통신 (HTTPS)
        ));
        
        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);
        
        // 노출할 헤더 (클라이언트에서 접근 가능한 헤더)
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        ));
        
        // 프리플라이트 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
