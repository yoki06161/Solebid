package com.sesac.solbid.service;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.user.response.LoginResponse;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.exception.OAuth2Exception;
import com.sesac.solbid.service.auth.OAuth2Service;
import com.sesac.solbid.service.auth.OAuth2StateService;
import com.sesac.solbid.service.auth.OAuth2UrlGenerator;
import com.sesac.solbid.service.user.UserService;
import com.sesac.solbid.util.JwtUtil;
import com.sesac.solbid.dto.auth.response.AuthUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2Service 단위 테스트")
class OAuth2ServiceTest {

    @Mock
    private InMemoryClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OAuth2StateService stateService;

    @Mock
    private OAuth2UrlGenerator urlGenerator;

    @InjectMocks
    private OAuth2Service oAuth2Service;

    private User testUser;
    private ClientRegistration googleClientRegistration;
    private ClientRegistration kakaoClientRegistration;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .name("Test User")
                .phone("01012345678")
                .build();
        
        // 테스트를 위해 리플렉션으로 userId 설정
        try {
            java.lang.reflect.Field userIdField = User.class.getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.set(testUser, 1L);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }

        // Google ClientRegistration 설정
        googleClientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("google-client-id")
                .clientSecret("google-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:5173/auth/callback/google")
                .scope("profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v2/userinfo")
                .userNameAttributeName("sub")
                .build();

        // Kakao ClientRegistration 설정
        kakaoClientRegistration = ClientRegistration.withRegistrationId("kakao")
                .clientId("kakao-client-id")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:5173/auth/callback/kakao")
                .scope("profile_nickname", "account_email")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .build();
    }

    @Test
    @DisplayName("OAuth2 인증 URL 생성 성공 - Google")
    void generateAuthUrl_Success_Google() {
        // Given
        String provider = "google";
        String expectedState = "test-state-123";
        String expectedAuthUrl = "https://accounts.google.com/o/oauth2/auth?client_id=google-client-id&state=" + expectedState;

        when(stateService.generateState()).thenReturn(expectedState);
        when(urlGenerator.generateAuthUrl(provider, expectedState)).thenReturn(expectedAuthUrl);

        // When
        AuthUrlResponse response = oAuth2Service.generateAuthUrl(provider);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAuthUrl()).isEqualTo(expectedAuthUrl);
        assertThat(response.getState()).isEqualTo(expectedState);
        assertThat(response.getProvider()).isEqualTo(provider);

        verify(stateService).generateState();
        verify(urlGenerator).generateAuthUrl(provider, expectedState);
    }

    @Test
    @DisplayName("OAuth2 인증 URL 생성 성공 - Kakao")
    void generateAuthUrl_Success_Kakao() {
        // Given
        String provider = "kakao";
        String expectedState = "test-state-456";
        String expectedAuthUrl = "https://kauth.kakao.com/oauth/authorize?client_id=kakao-client-id&state=" + expectedState;

        when(stateService.generateState()).thenReturn(expectedState);
        when(urlGenerator.generateAuthUrl(provider, expectedState)).thenReturn(expectedAuthUrl);

        // When
        AuthUrlResponse response = oAuth2Service.generateAuthUrl(provider);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAuthUrl()).isEqualTo(expectedAuthUrl);
        assertThat(response.getState()).isEqualTo(expectedState);
        assertThat(response.getProvider()).isEqualTo(provider);
    }

    @Test
    @DisplayName("OAuth2 인증 URL 생성 실패 - 지원하지 않는 Provider")
    void generateAuthUrl_Fail_UnsupportedProvider() {
        // Given
        String provider = "unsupported";
        String state = "test-state";

        when(stateService.generateState()).thenReturn(state);
        when(urlGenerator.generateAuthUrl(provider, state))
                .thenThrow(new OAuth2Exception(ErrorCode.INVALID_OAUTH2_PROVIDER));

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.generateAuthUrl(provider))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_OAUTH2_PROVIDER);
    }

    @Test
    @DisplayName("OAuth2 콜백 처리 성공 - Google")
    void processCallback_Success_Google() {
        // Given
        String provider = "google";
        String authCode = "test-auth-code";
        String state = "test-state";

        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(googleClientRegistration);
        // stateService.consumeState는 정상 동작 (void) 가정
        doNothing().when(stateService).consumeState(state);

        // When & Then - 실제 HTTP 통신이 발생하므로 예외가 발생할 것으로 예상
        assertThatThrownBy(() -> oAuth2Service.processCallback(provider, authCode, state))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(stateService).consumeState(state);
    }

    @Test
    @DisplayName("OAuth2 콜백 처리 실패 - State 검증 실패")
    void processCallback_Fail_InvalidState() {
        // Given
        String provider = "google";
        String authCode = "test-auth-code";
        String invalidState = "invalid-state";

        doThrow(new OAuth2Exception(ErrorCode.OAUTH2_STATE_MISMATCH))
                .when(stateService).consumeState(invalidState);

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.processCallback(provider, authCode, invalidState))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_STATE_MISMATCH);

        verify(stateService).consumeState(invalidState);
        verify(userService, never()).saveOrUpdate(any(), any());
    }

    @Test
    @DisplayName("OAuth2 콜백 처리 실패 - 지원하지 않는 Provider")
    void processCallback_Fail_UnsupportedProvider() {
        // Given
        String provider = "unsupported";
        String authCode = "test-auth-code";
        String state = "test-state";

        doNothing().when(stateService).consumeState(state);
        when(clientRegistrationRepository.findByRegistrationId("unsupported")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.processCallback(provider, authCode, state))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_OAUTH2_PROVIDER);

        verify(stateService).consumeState(state);
    }

    @Test
    @DisplayName("일반 로그인 처리 실패 - 실제 HTTP 통신으로 인한 토큰 에러")
    void login_Fail_TokenError_Google() {
        // Given
        String provider = "google";
        String authCode = "test-auth-code";

        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(googleClientRegistration);

        // When & Then - 실제 HTTP 통신이 발생하므로 토큰 에러가 발생할 것으로 예상
        assertThatThrownBy(() -> oAuth2Service.login(provider, authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(clientRegistrationRepository).findByRegistrationId("google");
        verify(userService, never()).saveOrUpdate(any(), any());
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("일반 로그인 처리 실패 - 지원하지 않는 Provider")
    void login_Fail_UnsupportedProvider() {
        // Given
        String provider = "unsupported";
        String authCode = "test-auth-code";

        when(clientRegistrationRepository.findByRegistrationId("unsupported")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.login(provider, authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_OAUTH2_PROVIDER);

        verify(clientRegistrationRepository).findByRegistrationId("unsupported");
        verify(userService, never()).saveOrUpdate(any(), any());
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("OAuth2 콜백 처리 실패 - State 검증 후 로그인 실패")
    void processCallback_Fail_LoginFailAfterStateValidation() {
        // Given
        String provider = "google";
        String authCode = "invalid-auth-code";
        String state = "valid-state";

        doNothing().when(stateService).consumeState(state);
        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.processCallback(provider, authCode, state))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_OAUTH2_PROVIDER);

        verify(stateService).consumeState(state);
    }



    @Test
    @DisplayName("사용자 정보 동기화 테스트 - HTTP 통신 실패")
    void userInfoSync_Fail_HttpError() {
        // Given
        String provider = "google";
        String authCode = "test-auth-code";

        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(googleClientRegistration);

        // When & Then - 실제 HTTP 통신이 발생하므로 에러가 발생할 것으로 예상
        assertThatThrownBy(() -> oAuth2Service.login(provider, authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(clientRegistrationRepository).findByRegistrationId("google");
        verify(userService, never()).saveOrUpdate(any(), any());
    }

    @Test
    @DisplayName("사용자 정보 동기화 테스트 - Kakao HTTP 통신 실패")
    void userInfoSync_Fail_KakaoHttpError() {
        // Given
        String provider = "kakao";
        String authCode = "test-auth-code";

        when(clientRegistrationRepository.findByRegistrationId("kakao")).thenReturn(kakaoClientRegistration);

        // When & Then - 실제 HTTP 통신이 발생하므로 에러가 발생할 것으로 예상
        assertThatThrownBy(() -> oAuth2Service.login(provider, authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(clientRegistrationRepository).findByRegistrationId("kakao");
        verify(userService, never()).saveOrUpdate(any(), any());
    }

    @Test
    @DisplayName("WebClient 응답 예외 처리 테스트")
    void webClientResponseException_Test() {
        // Given
        String provider = "google";
        String authCode = "test-auth-code";

        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(googleClientRegistration);

        // When & Then - 실제 HTTP 통신에서 WebClientResponseException 발생
        assertThatThrownBy(() -> oAuth2Service.login(provider, authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(clientRegistrationRepository).findByRegistrationId("google");
    }

    @Test
    @DisplayName("HTTP 통신 실패 시 예외 처리 - 후속 서비스 호출 없음 검증")
    void httpCommunication_Fail_Test() {
        // Given
        String provider = "google";
        String authCode = "test-auth-code";

        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(googleClientRegistration);

        // When & Then - HTTP 통신에서 실패하므로 OAuth2Exception이 발생
        assertThatThrownBy(() -> oAuth2Service.login(provider, authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(clientRegistrationRepository).findByRegistrationId("google");
        // HTTP 통신 실패로 인해 후속 서비스들은 호출되지 않음을 검증
        verify(userService, never()).saveOrUpdate(any(), any());
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("OAuth2 콜백 처리 - 예외 발생 시 State 삭제 확인")
    void processCallback_ExceptionOccurred_StateRemoved() {
        // Given
        String provider = "google";
        String authCode = "test-auth-code";
        String state = "test-state";

        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(googleClientRegistration);
        doNothing().when(stateService).consumeState(state);

        // When & Then - 실제 HTTP 통신에서 예외 발생
        assertThatThrownBy(() -> oAuth2Service.processCallback(provider, authCode, state))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(stateService).consumeState(state);
    }

    @Test
    @DisplayName("다양한 Provider 테스트 - 대소문자 구분 없음")
    void provider_CaseInsensitive_Test() {
        // Given
        String provider = "GOOGLE"; // 대문자
        String authCode = "test-auth-code";

        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(googleClientRegistration);

        // When & Then - 실제 HTTP 통신에서 예외 발생
        assertThatThrownBy(() -> oAuth2Service.login(provider, authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(clientRegistrationRepository).findByRegistrationId("google"); // 소문자로 조회됨
        verify(userService, never()).saveOrUpdate(any(), any());
    }

    @Test
    @DisplayName("LoginResponse 생성 테스트")
    void createLoginResponse_Success() {
        // Given
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-456";

        // When
        LoginResponse response = LoginResponse.from(testUser, accessToken, refreshToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(testUser.getUserId());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getNickname()).isEqualTo(testUser.getNickname());
        assertThat(response.getUserType()).isEqualTo(testUser.getUserType());
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
    }
}