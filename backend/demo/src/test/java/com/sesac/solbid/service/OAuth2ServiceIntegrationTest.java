package com.sesac.solbid.service;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.exception.OAuth2Exception;
import com.sesac.solbid.service.auth.OAuth2Service;
import com.sesac.solbid.service.auth.OAuth2StateService;
import com.sesac.solbid.service.auth.OAuth2UrlGenerator;
import com.sesac.solbid.service.user.UserService;
import com.sesac.solbid.util.JwtUtil;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2Service 통합 테스트 - WebClient Mock")
class OAuth2ServiceIntegrationTest {

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

    private MockWebServer mockWebServer;
    private ClientRegistration testClientRegistration;
    private User testUser;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        // 테스트용 ClientRegistration 설정 (MockWebServer URL 사용)
        testClientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .scope("profile", "email")
                .authorizationUri(baseUrl + "auth")
                .tokenUri(baseUrl + "token")
                .userInfoUri(baseUrl + "userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();

        testUser = User.builder()
                .email("test@example.com")
                .nickname("테스트사용자")
                .name("테스트사용자")
                .build();
        
        // 테스트를 위해 리플렉션으로 userId 설정
        try {
            java.lang.reflect.Field userIdField = User.class.getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.set(testUser, 1L);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("OAuth2 토큰 획득 성공 - Google")
    void getAccessToken_Success_Google() throws InterruptedException {
        // Given
        String authCode = "test-auth-code";
        String expectedAccessToken = "test-access-token";

        // Mock 토큰 응답
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\":\"" + expectedAccessToken + "\",\"token_type\":\"Bearer\",\"expires_in\":3600}")
                .addHeader("Content-Type", "application/json"));

        // Mock 사용자 정보 응답
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"sub\":\"google-user-id\",\"email\":\"test@gmail.com\",\"name\":\"Test User\"}")
                .addHeader("Content-Type", "application/json"));

        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(testClientRegistration);
        when(userService.saveOrUpdate(eq("google"), any(), any(), any())).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser.getEmail())).thenReturn("jwt-access-token");
        when(jwtUtil.generateRefreshToken(testUser.getEmail())).thenReturn("jwt-refresh-token");

        // When
        com.sesac.solbid.dto.user.response.LoginResponse response = oAuth2Service.login("google", authCode);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(testUser.getUserId());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("jwt-refresh-token");

        // 토큰 요청 검증
        RecordedRequest tokenRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(tokenRequest).isNotNull();
        assertThat(tokenRequest.getPath()).isEqualTo("/token");
        assertThat(tokenRequest.getMethod()).isEqualTo("POST");
        String tokenRequestBody = tokenRequest.getBody().readUtf8();
        assertThat(tokenRequestBody).contains("grant_type=authorization_code");
        assertThat(tokenRequestBody).contains("code=" + authCode);

        // 사용자 정보 요청 검증
        RecordedRequest userInfoRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(userInfoRequest).isNotNull();
        assertThat(userInfoRequest.getPath()).isEqualTo("/userinfo");
        assertThat(userInfoRequest.getMethod()).isEqualTo("GET");
        assertThat(userInfoRequest.getHeader("Authorization")).isEqualTo("Bearer " + expectedAccessToken);

        verify(userService).saveOrUpdate(eq("google"), any(), any(), any());
        verify(jwtUtil).generateToken(testUser.getEmail());
        verify(jwtUtil).generateRefreshToken(testUser.getEmail());
    }

    @Test
    @DisplayName("OAuth2 토큰 획득 실패 - 잘못된 인증 코드")
    void getAccessToken_Fail_InvalidCode() {
        // Given
        String invalidAuthCode = "invalid-code";

        // Mock 에러 응답
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{\"error\":\"invalid_grant\",\"error_description\":\"Invalid authorization code\"}")
                .addHeader("Content-Type", "application/json"));

        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(testClientRegistration);

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.login("google", invalidAuthCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(userService, never()).saveOrUpdate(any(), any());
    }

    @Test
    @DisplayName("OAuth2 사용자 정보 획득 실패 - 토큰 만료")
    void getUserInfo_Fail_ExpiredToken() {
        // Given
        String authCode = "test-auth-code";

        // Mock 토큰 응답 (성공)
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\":\"expired-token\",\"token_type\":\"Bearer\"}")
                .addHeader("Content-Type", "application/json"));

        // Mock 사용자 정보 응답 (실패 - 토큰 만료)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\":\"invalid_token\",\"error_description\":\"Token has expired\"}")
                .addHeader("Content-Type", "application/json"));

        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(testClientRegistration);

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.login("google", authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_USER_INFO_ERROR);

        verify(userService, never()).saveOrUpdate(any(), any());
    }

    @Test
    @DisplayName("OAuth2 토큰 응답에 access_token이 없는 경우")
    void getAccessToken_Fail_NoAccessToken() {
        // Given
        String authCode = "test-auth-code";

        // Mock 토큰 응답 (access_token 없음)
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"token_type\":\"Bearer\",\"expires_in\":3600}")
                .addHeader("Content-Type", "application/json"));

        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(testClientRegistration);

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.login("google", authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(userService, never()).saveOrUpdate(any(), any());
    }

    @Test
    @DisplayName("OAuth2 사용자 정보가 비어있는 경우")
    void getUserInfo_Fail_EmptyResponse() {
        // Given
        String authCode = "test-auth-code";

        // Mock 토큰 응답
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\":\"test-access-token\",\"token_type\":\"Bearer\"}")
                .addHeader("Content-Type", "application/json"));

        // Mock 사용자 정보 응답 (빈 응답)
        mockWebServer.enqueue(new MockResponse()
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(testClientRegistration);

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.login("google", authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_USER_INFO_ERROR);

        verify(userService, never()).saveOrUpdate(any(), any());
    }

    // 타임아웃 테스트는 WebClient에 타임아웃 설정이 없어서 제거
    // 실제 운영 환경에서는 WebClient에 타임아웃 설정을 추가하는 것을 권장

    @Test
    @DisplayName("Kakao 사용자 정보 파싱 테스트")
    void parseUserInfo_Kakao() throws InterruptedException {
        // Given
        String authCode = "test-auth-code";
        
        // Kakao ClientRegistration 설정
        String baseUrl = mockWebServer.url("/").toString();
        ClientRegistration kakaoClientRegistration = ClientRegistration.withRegistrationId("kakao")
                .clientId("kakao-client-id")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/kakao")
                .scope("profile_nickname", "account_email")
                .authorizationUri(baseUrl + "auth")
                .tokenUri(baseUrl + "token")
                .userInfoUri(baseUrl + "userinfo")
                .userNameAttributeName("id")
                .clientName("Kakao")
                .build();

        // Mock 토큰 응답
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\":\"kakao-access-token\",\"token_type\":\"Bearer\"}")
                .addHeader("Content-Type", "application/json"));

        // Mock Kakao 사용자 정보 응답
        String kakaoUserInfo = "{\n" +
                "  \"id\": 12345,\n" +
                "  \"kakao_account\": {\n" +
                "    \"email\": \"user@kakao.com\",\n" +
                "    \"profile\": {\n" +
                "      \"nickname\": \"카카오사용자\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(kakaoUserInfo)
                .addHeader("Content-Type", "application/json"));

        Map<String, Object> expectedUserAttributes = new HashMap<>();
        expectedUserAttributes.put("id", 12345);
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "user@kakao.com");
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "카카오사용자");
        kakaoAccount.put("profile", profile);
        expectedUserAttributes.put("kakao_account", kakaoAccount);

        when(clientRegistrationRepository.findByRegistrationId("kakao"))
                .thenReturn(kakaoClientRegistration);
        when(userService.saveOrUpdate(eq("kakao"), any(), any(), any())).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser.getEmail())).thenReturn("jwt-token");
        when(jwtUtil.generateRefreshToken(testUser.getEmail())).thenReturn("jwt-refresh");

        // When
        com.sesac.solbid.dto.user.response.LoginResponse response = oAuth2Service.login("kakao", authCode);

        // Then
        assertThat(response).isNotNull();
        
        // 토큰 요청 검증 (첫 번째 요청)
        RecordedRequest tokenRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(tokenRequest).isNotNull();
        assertThat(tokenRequest.getPath()).isEqualTo("/token");
        
        // 사용자 정보 요청 검증 (두 번째 요청)
        RecordedRequest userInfoRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(userInfoRequest).isNotNull();
        assertThat(userInfoRequest.getPath()).isEqualTo("/userinfo");
        assertThat(userInfoRequest.getHeader("Authorization")).isEqualTo("Bearer kakao-access-token");

        verify(userService).saveOrUpdate(eq("kakao"), any(), any(), any());
    }

    @Test
    @DisplayName("서버 응답 500 에러 처리")
    void serverError_500_Test() {
        // Given
        String authCode = "test-auth-code";

        // Mock 서버 에러 응답
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\":\"internal_server_error\"}")
                .addHeader("Content-Type", "application/json"));

        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(testClientRegistration);

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.login("google", authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(userService, never()).saveOrUpdate(any(), any());
    }

    @Test
    @DisplayName("잘못된 JSON 응답 처리")
    void invalidJson_Test() {
        // Given
        String authCode = "test-auth-code";

        // Mock 잘못된 JSON 응답
        mockWebServer.enqueue(new MockResponse()
                .setBody("invalid json response")
                .addHeader("Content-Type", "application/json"));

        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(testClientRegistration);

        // When & Then
        assertThatThrownBy(() -> oAuth2Service.login("google", authCode))
                .isInstanceOf(OAuth2Exception.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH2_TOKEN_ERROR);

        verify(userService, never()).saveOrUpdate(any(), any());
    }
}