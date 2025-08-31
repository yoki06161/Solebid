package com.sesac.solbid.service;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.exception.OAuth2Exception;
import com.sesac.solbid.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import com.sesac.solbid.dto.auth.response.AuthUrlResponse;
import com.sesac.solbid.dto.UserDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2Service {

    private final InMemoryClientRegistrationRepository clientRegistrationRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final OAuth2StateService stateService;
    private final OAuth2UrlGenerator urlGenerator;
    
    // WebClient 최적화 설정
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
            .build();

    /**
     * OAuth2 인증 URL 생성
     * @param provider 소셜 플랫폼 이름
     * @return 인증 URL과 state 정보
     */
    public AuthUrlResponse generateAuthUrl(String provider) {
        log.debug("OAuth2 인증 URL 생성 요청: provider={}", provider);
        
        // State 생성
        String state = stateService.generateState();
        
        // 인증 URL 생성
        String authUrl = urlGenerator.generateAuthUrl(provider, state);
        
        log.info("OAuth2 인증 URL 생성 완료: provider={}", provider);
        return AuthUrlResponse.of(authUrl, state, provider);
    }

    /**
     * OAuth2 콜백 처리 (State 검증 포함)
     * @param providerName 소셜 플랫폼 이름
     * @param authCode 인증 코드
     * @param state State 파라미터
     * @return 로그인 응답
     */
    @Transactional
    public UserDto.LoginResponse processCallback(String providerName, String authCode, String state) {
        log.debug("OAuth2 콜백 처리 시작: provider={}", providerName);
        try {
            stateService.validateState(state);
            UserDto.LoginResponse response = login(providerName, authCode);
            log.info("OAuth2 콜백 처리 완료: provider={}, userId={}", providerName, response.getUserId());
            return response;
        } finally {
            stateService.removeState(state);
        }
    }

    /**
     * 기존 로그인 메서드 (내부 사용)
     */
    @Transactional
    public UserDto.LoginResponse login(String providerName, String authCode) {
        log.debug("OAuth2 로그인 처리 시작: provider={}", providerName);
        
        try {
            ClientRegistration provider = getClientRegistration(providerName);
            Map<String, Object> tokenResp = getTokenResponse(provider, authCode);
            String accessToken = (String) tokenResp.get("access_token");
            String refreshToken = (String) tokenResp.get("refresh_token");
            Map<String, Object> userAttributes = getUserAttributes(provider, accessToken);
            
            // 필수 사용자 정보 검증 (Google은 email 필수)
            String regId = provider.getRegistrationId().toLowerCase();
            if ("google".equals(regId)) {
                Object emailObj = userAttributes.get("email");
                if (emailObj == null || String.valueOf(emailObj).isBlank()) {
                    throw new OAuth2Exception(ErrorCode.OAUTH2_USER_INFO_ERROR);
                }
            }

            // 사용자 정보 동기화 + 토큰 저장
            User user = userService.saveOrUpdate(providerName, userAttributes, accessToken, refreshToken);

            // JWT 토큰 생성
            final String serviceAccessToken = jwtUtil.generateToken(user.getEmail());
            final String serviceRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            log.info("OAuth2 로그인 완료: provider={}, userId={}, email={}", 
                    providerName, user.getUserId(), maskEmail(user.getEmail()));
            
            return UserDto.LoginResponse.from(user, serviceAccessToken, serviceRefreshToken);
            
        } catch (WebClientResponseException e) {
            log.error("OAuth2 통신 오류: provider={}, status={}, body=",
                    providerName, e.getStatusCode());
            log.error("{}", maskSensitiveData(e.getResponseBodyAsString()));
            throw new OAuth2Exception(ErrorCode.OAUTH2_TOKEN_ERROR);
        } catch (CustomException e) {
            log.warn("OAuth2 로그인 비즈니스 예외: provider={}, error={}", providerName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 오류 발생: provider={}", providerName, e);
            throw e;
        }
    }


    /**
     * ClientRegistration 조회 (에러 처리 포함)
     */
    private ClientRegistration getClientRegistration(String providerName) {
        ClientRegistration provider = clientRegistrationRepository
                .findByRegistrationId(providerName.toLowerCase());
        
        if (provider == null) {
            log.warn("지원하지 않는 OAuth2 provider: {}", providerName);
            throw new OAuth2Exception(ErrorCode.INVALID_OAUTH2_PROVIDER);
        }
        
        return provider;
    }

    /**
     * OAuth2 액세스 토큰 획득 (개선된 에러 처리 및 WebClient 최적화)
     */
    private String getAccessToken(ClientRegistration provider, String authCode) {
        Map<String, Object> response = getTokenResponse(provider, authCode);
        String accessToken = (String) response.get("access_token");
        if (accessToken == null || accessToken.isBlank()) {
            log.error("OAuth2 토큰 응답에 access_token이 없음: provider={}", provider.getRegistrationId());
            throw new OAuth2Exception(ErrorCode.OAUTH2_TOKEN_ERROR);
        }
        log.debug("OAuth2 액세스 토큰 획득 성공: provider={}", provider.getRegistrationId());
        return accessToken;
    }

    /**
     * 토큰 응답 전체를 반환 (access_token, refresh_token 등)
     */
    private Map<String, Object> getTokenResponse(ClientRegistration provider, String authCode) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", provider.getClientId());
        formData.add("client_secret", provider.getClientSecret());
        formData.add("redirect_uri", provider.getRedirectUri());
        formData.add("code", authCode);

        try {
            Map<String, Object> response = webClient
                    .post()
                    .uri(provider.getProviderDetails().getTokenUri())
                    .headers(header -> {
                        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                        header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                    })
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || !response.containsKey("access_token")) {
                log.error("OAuth2 토큰 응답이 비정상: provider={}", provider.getRegistrationId());
                throw new OAuth2Exception(ErrorCode.OAUTH2_TOKEN_ERROR);
            }

            // refresh_token은 상황에 따라 없을 수 있음(기존 동의 등)
            if (response.get("refresh_token") == null) {
                log.debug("refresh_token 미수신: provider={}", provider.getRegistrationId());
            }
            return response;
        } catch (WebClientResponseException e) {
            log.error("OAuth2 토큰 요청 실패: provider={}, status={}, body=",
                    provider.getRegistrationId(), e.getStatusCode());
            log.error("{}", maskSensitiveData(e.getResponseBodyAsString()));
            throw new OAuth2Exception(ErrorCode.OAUTH2_TOKEN_ERROR);
        } catch (Exception e) {
            log.error("OAuth2 토큰 획득 중 예외 발생: provider={}", provider.getRegistrationId(), e);
            throw new OAuth2Exception(ErrorCode.OAUTH2_TOKEN_ERROR);
        }
    }

    /**
     * OAuth2 사용자 정보 획득 (개선된 에러 처리)
     */
    private Map<String, Object> getUserAttributes(ClientRegistration provider, String accessToken) {
        try {
            Map<String, Object> userAttributes = webClient
                    .get()
                    .uri(provider.getProviderDetails().getUserInfoEndpoint().getUri())
                    .headers(header -> header.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(10)) // 10초 타임아웃
                    .block();

            if (userAttributes == null || userAttributes.isEmpty()) {
                log.error("OAuth2 사용자 정보가 비어있음: provider={}", provider.getRegistrationId());
                throw new OAuth2Exception(ErrorCode.OAUTH2_USER_INFO_ERROR);
            }

            log.debug("OAuth2 사용자 정보 획득 성공: provider={}", provider.getRegistrationId());
            return userAttributes;
            
        } catch (WebClientResponseException e) {
            log.error("OAuth2 사용자 정보 요청 실패: provider={}, status={}, body=",
                    provider.getRegistrationId(), e.getStatusCode());
            log.error("{}", maskSensitiveData(e.getResponseBodyAsString()));
            throw new OAuth2Exception(ErrorCode.OAUTH2_USER_INFO_ERROR);
        } catch (Exception e) {
            log.error("OAuth2 사용자 정보 획득 중 예외 발생: provider={}", provider.getRegistrationId(), e);
            throw new OAuth2Exception(ErrorCode.OAUTH2_USER_INFO_ERROR);
        }
    }

    /**
     * 이메일 마스킹 처리 (보안)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return "**@" + domain;
        }
        
        return localPart.substring(0, 2) + "****@" + domain;
    }

    /**
     * 민감한 데이터 마스킹 처리 (토큰, 개인정보 등)
     */
    private String maskSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return "****";
        }
        
        // JSON 응답에서 민감한 필드들 마스킹
        String maskedData = data
                .replaceAll("\"access_token\"\\s*:\\s*\"[^\"]+\"", "\"access_token\":\"****\"")
                .replaceAll("\"refresh_token\"\\s*:\\s*\"[^\"]+\"", "\"refresh_token\":\"****\"")
                .replaceAll("\"client_secret\"\\s*:\\s*\"[^\"]+\"", "\"client_secret\":\"****\"")
                .replaceAll("\"code\"\\s*:\\s*\"[^\"]+\"", "\"code\":\"****\"")
                .replaceAll("\"email\"\\s*:\\s*\"([^@\"]+)@([^\"]+)\"", "\"email\":\"**@$2\"")
                .replaceAll("\"phone\"\\s*:\\s*\"[^\"]+\"", "\"phone\":\"****\"");
        
        // 응답이 너무 길면 잘라내기
        if (maskedData.length() > 500) {
            return maskedData.substring(0, 500) + "... [truncated]";
        }
        
        return maskedData;
    }
}
