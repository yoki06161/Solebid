package com.sesac.solbid.service;

import com.sesac.solbid.exception.OAuth2Exception;
import com.sesac.solbid.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth2 인증 URL 생성기
 * 각 소셜 플랫폼별 인증 URL을 생성하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2UrlGenerator {

    private final InMemoryClientRegistrationRepository clientRegistrationRepository;

    /**
     * 소셜 플랫폼별 OAuth2 인증 URL 생성
     * @param provider 소셜 플랫폼 이름 (google, kakao)
     * @param state CSRF 방지용 state 파라미터
     * @return 완성된 OAuth2 인증 URL
     * @throws OAuth2Exception 지원하지 않는 provider인 경우
     */
    public String generateAuthUrl(String provider, String state) {
        if (provider == null || provider.trim().isEmpty()) {
            log.warn("OAuth2 URL 생성 실패: provider가 null 또는 빈 값");
            throw new OAuth2Exception(ErrorCode.INVALID_OAUTH2_PROVIDER);
        }

        String normalizedProvider = provider.toLowerCase().trim();
        
        ClientRegistration clientRegistration = clientRegistrationRepository
                .findByRegistrationId(normalizedProvider);
        
        if (clientRegistration == null) {
            log.warn("OAuth2 URL 생성 실패: 지원하지 않는 provider - {}", provider);
            throw new OAuth2Exception(ErrorCode.INVALID_OAUTH2_PROVIDER);
        }

        String authUrl = buildAuthorizationUrl(clientRegistration, state);
        
        log.debug("OAuth2 인증 URL 생성 완료: provider={}, state={}", 
                provider, maskState(state));
        
        return authUrl;
    }

    /**
     * ClientRegistration 정보를 기반으로 인증 URL 구성
     */
    private String buildAuthorizationUrl(ClientRegistration clientRegistration, String state) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(clientRegistration.getProviderDetails().getAuthorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", clientRegistration.getClientId())
                .queryParam("redirect_uri", clientRegistration.getRedirectUri())
                .queryParam("state", state);

        // scope 파라미터 추가
        if (!clientRegistration.getScopes().isEmpty()) {
            String scopes = String.join(" ", clientRegistration.getScopes());
            builder.queryParam("scope", scopes);
        }

        // Kakao의 경우 추가 파라미터 설정
        if ("kakao".equals(clientRegistration.getRegistrationId())) {
            // Kakao는 scope를 쉼표로 구분
            if (!clientRegistration.getScopes().isEmpty()) {
                String kakaoScopes = String.join(",", clientRegistration.getScopes());
                builder.replaceQueryParam("scope", kakaoScopes);
            }
        }

        // Google refresh_token 발급 유도
        if ("google".equals(clientRegistration.getRegistrationId())) {
            builder.queryParam("access_type", "offline");
            builder.queryParam("prompt", "consent");
        }

        return builder.build().toUriString();
    }



    /**
     * 보안을 위해 state 값을 마스킹 처리
     */
    private String maskState(String state) {
        if (state == null || state.length() < 8) {
            return "****";
        }
        return state.substring(0, 4) + "****" + state.substring(state.length() - 4);
    }
}