package com.sesac.solbid.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.ProviderType;
import com.sesac.solbid.domain.enums.UserType;
import com.sesac.solbid.repository.SocialLoginRepository;
import com.sesac.solbid.repository.UserRepository;
import com.sesac.solbid.service.OAuth2StateService;
import com.sesac.solbid.dto.auth.response.AuthUrlResponse;
import com.sesac.solbid.dto.auth.request.CallbackRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OAuth2 전체 플로우 End-to-End 통합 테스트
 * 
 * Mock OAuth2 서버를 사용하여 실제 OAuth2 플로우를 시뮬레이션합니다.
 * - 인증 URL 생성부터 콜백 처리까지 전체 플로우 테스트
 * - Google과 Kakao 각각에 대한 완전한 시나리오 테스트
 * - 예외 상황 발생 시 전체 플로우 테스트
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("OAuth2 End-to-End 통합 테스트")
class OAuth2EndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocialLoginRepository socialLoginRepository;

    private static MockWebServer mockGoogleServer;
    private static MockWebServer mockKakaoServer;

    @BeforeAll
    static void setUpMockServers() {
        // Servers will be started lazily in DynamicPropertySource to ensure availability during property resolution
    }

    @AfterAll
    static void tearDownMockServers() throws IOException {
        if (mockGoogleServer != null) {
            mockGoogleServer.shutdown();
        }
        if (mockKakaoServer != null) {
            mockKakaoServer.shutdown();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Ensure MockWebServers are started before their URLs are used
        try {
            if (mockGoogleServer == null) {
                mockGoogleServer = new MockWebServer();
                mockGoogleServer.start();
            }
            if (mockKakaoServer == null) {
                mockKakaoServer = new MockWebServer();
                mockKakaoServer.start();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start MockWebServers", e);
        }

        // --- Datasource (H2 in-memory) ---
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:solebid_e2e;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=USER");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");

        // --- JPA & Hibernate for H2 ---
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("spring.jpa.properties.hibernate.globally_quoted_identifiers", () -> "false");

        // --- Google OAuth2 Provider 설정을 Mock 서버로 변경 ---
        registry.add("spring.security.oauth2.client.provider.google.token-uri",
                () -> mockGoogleServer.url("/oauth/token").toString());
        registry.add("spring.security.oauth2.client.provider.google.user-info-uri",
                () -> mockGoogleServer.url("/oauth2/v2/userinfo").toString());

        // --- Kakao OAuth2 Provider 설정을 Mock 서버로 변경 ---
        registry.add("spring.security.oauth2.client.provider.kakao.token-uri",
                () -> mockKakaoServer.url("/oauth/token").toString());
        registry.add("spring.security.oauth2.client.provider.kakao.user-info-uri",
                () -> mockKakaoServer.url("/v2/user/me").toString());
    }

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 데이터베이스 정리
        socialLoginRepository.deleteAll();
        userRepository.deleteAll();

        // 각 테스트 전에 MockWebServer의 기록된 요청 큐를 비움으로써 테스트 간 간섭 제거
        clearRecordedRequests(mockGoogleServer);
        clearRecordedRequests(mockKakaoServer);
    }

    // MockWebServer의 RecordedRequest 큐를 비우는 유틸리티
    private void clearRecordedRequests(MockWebServer server) {
        if (server == null) return;
        try {
            while (true) {
                RecordedRequest req = server.takeRequest(100, TimeUnit.MILLISECONDS);
                if (req == null) break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // === Google OAuth2 전체 플로우 테스트 ===

    @Test
    @Order(1)
    @DisplayName("Google OAuth2 전체 플로우 - 신규 사용자 성공 시나리오")
    @Transactional
    void googleOAuth2FullFlow_NewUser_Success() throws Exception {
        // === 1단계: 인증 URL 생성 ===
        MvcResult authUrlResult = mockMvc.perform(get("/api/auth/oauth2/google/url")
                        .header("User-Agent", "Mozilla/5.0 (Test Browser)")
                        .header("X-Forwarded-For", "192.168.1.100"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OAuth2 인증 URL이 생성되었습니다."))
                .andExpect(jsonPath("$.data.authUrl").exists())
                .andExpect(jsonPath("$.data.state").exists())
                .andExpect(jsonPath("$.data.provider").value("google"))
                .andReturn();

        // 응답에서 state 추출
        String responseContent = authUrlResult.getResponse().getContentAsString();
        AuthUrlResponse authUrlResponse = objectMapper.readValue(
            objectMapper.readTree(responseContent).get("data").toString(),
            AuthUrlResponse.class
        );
        String state = authUrlResponse.getState();
        
        assertThat(state).isNotNull().isNotEmpty();
        assertThat(authUrlResponse.getAuthUrl()).contains("accounts.google.com");
        assertThat(authUrlResponse.getAuthUrl()).contains("state=" + state);

        // === 2단계: Mock Google 서버 응답 설정 ===
        // 토큰 교환 응답
        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "access_token": "mock-google-access-token",
                    "token_type": "Bearer",
                    "expires_in": 3600,
                    "scope": "profile email"
                }
                """));

        // 사용자 정보 응답
        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "id": "google-user-123",
                    "email": "newuser@gmail.com",
                    "name": "구글 신규사용자",
                    "picture": "https://lh3.googleusercontent.com/a/default-user"
                }
                """));

        // === 3단계: OAuth2 콜백 처리 ===
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .code("mock-authorization-code")
                .state(state)
                .build();

        MvcResult callbackResult = mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest))
                        .header("User-Agent", "Mozilla/5.0 (Test Browser)")
                        .header("X-Forwarded-For", "192.168.1.100"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("소셜로그인이 완료되었습니다."))
                .andExpect(jsonPath("$.data.email").value("newuser@gmail.com"))
                .andExpect(jsonPath("$.data.nickname").value(org.hamcrest.Matchers.startsWith("user_")))
                .andExpect(jsonPath("$.data.userType").value("USER"))
                .andExpect(jsonPath("$.data.requiresNickname").value(true))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        // === 4단계: 데이터베이스 검증 ===
        // 사용자가 생성되었는지 확인
        User createdUser = userRepository.findByEmail("newuser@gmail.com").orElse(null);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getNickname()).startsWith("user_");
        assertThat(createdUser.getUserType()).isEqualTo(UserType.USER);

        // 소셜 로그인 정보가 저장되었는지 확인
        var socialLogin = socialLoginRepository.findByProviderAndProviderId(ProviderType.Google, "google-user-123");
        assertThat(socialLogin).isPresent();
        assertThat(socialLogin.get().getUser().getUserId()).isEqualTo(createdUser.getUserId());

        // === 5단계: Mock 서버 요청 검증 ===
        // 토큰 요청 검증
        RecordedRequest tokenRequest = mockGoogleServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(tokenRequest).isNotNull();
        assertThat(tokenRequest.getPath()).isEqualTo("/oauth/token");
        assertThat(tokenRequest.getMethod()).isEqualTo("POST");
        assertThat(tokenRequest.getBody().readUtf8()).contains("code=mock-authorization-code");

        // 사용자 정보 요청 검증
        RecordedRequest userInfoRequest = mockGoogleServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(userInfoRequest).isNotNull();
        assertThat(userInfoRequest.getPath()).isEqualTo("/oauth2/v2/userinfo");
        assertThat(userInfoRequest.getMethod()).isEqualTo("GET");
        assertThat(userInfoRequest.getHeader("Authorization")).isEqualTo("Bearer mock-google-access-token");
    }

    @Test
    @Order(2)
    @DisplayName("Google OAuth2 전체 플로우 - 기존 사용자 로그인 및 정보 동기화")
    @Transactional
    void googleOAuth2FullFlow_ExistingUser_Success() throws Exception {
        // === 사전 조건: 기존 사용자 생성 ===
        User existingUser = User.builder()
                .email("existing@gmail.com")
                .nickname("기존사용자")
                .build();
        existingUser = userRepository.save(existingUser);

        // 기존 소셜 로그인 정보 생성
        socialLoginRepository.save(com.sesac.solbid.domain.SocialLogin.builder()
                .user(existingUser)
                .provider(ProviderType.Google)
                .providerId("google-existing-123")
                .build());

        // === 1단계: 인증 URL 생성 ===
        MvcResult authUrlResult = mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = authUrlResult.getResponse().getContentAsString();
        AuthUrlResponse authUrlResponse = objectMapper.readValue(
            objectMapper.readTree(responseContent).get("data").toString(),
            AuthUrlResponse.class
        );
        String state = authUrlResponse.getState();

        // === 2단계: Mock 서버 응답 설정 (업데이트된 사용자 정보) ===
        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "access_token": "mock-google-access-token-existing",
                    "token_type": "Bearer",
                    "expires_in": 3600
                }
                """));

        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "id": "google-existing-123",
                    "email": "existing@gmail.com",
                    "name": "업데이트된 구글사용자",
                    "picture": "https://lh3.googleusercontent.com/a/updated-user"
                }
                """));

        // === 3단계: OAuth2 콜백 처리 ===
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .code("mock-existing-user-code")
                .state(state)
                .build();

        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("existing@gmail.com"))
                .andExpect(jsonPath("$.data.nickname").value("기존사용자"))
                .andExpect(cookie().exists("accessToken"));

        // === 4단계: 사용자 정보 동기화 검증 ===
        User updatedUser = userRepository.findByEmail("existing@gmail.com").orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getNickname()).isEqualTo("기존사용자");
        assertThat(updatedUser.getUserId()).isEqualTo(existingUser.getUserId());
    }

    // === Kakao OAuth2 전체 플로우 테스트 ===

    @Test
    @Order(3)
    @DisplayName("Kakao OAuth2 전체 플로우 - 신규 사용자 성공 시나리오")
    @Transactional
    void kakaoOAuth2FullFlow_NewUser_Success() throws Exception {
        // === 1단계: 인증 URL 생성 ===
        MvcResult authUrlResult = mockMvc.perform(get("/api/auth/oauth2/kakao/url")
                        .header("User-Agent", "KakaoTalk Mobile App")
                        .header("X-Real-IP", "10.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.provider").value("kakao"))
                .andReturn();

        String responseContent = authUrlResult.getResponse().getContentAsString();
        AuthUrlResponse authUrlResponse = objectMapper.readValue(
            objectMapper.readTree(responseContent).get("data").toString(),
            AuthUrlResponse.class
        );
        String state = authUrlResponse.getState();

        assertThat(authUrlResponse.getAuthUrl()).contains("kauth.kakao.com");

        // === 2단계: Mock Kakao 서버 응답 설정 ===
        mockKakaoServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "access_token": "mock-kakao-access-token",
                    "token_type": "bearer",
                    "expires_in": 21599,
                    "scope": "profile_nickname account_email"
                }
                """));

        mockKakaoServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "id": 987654321,
                    "kakao_account": {
                        "email": "newuser@kakao.com",
                        "profile": {
                            "nickname": "카카오 신규사용자"
                        }
                    }
                }
                """));

        // === 3단계: OAuth2 콜백 처리 ===
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .code("mock-kakao-authorization-code")
                .state(state)
                .build();

        mockMvc.perform(post("/api/auth/oauth2/kakao/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest))
                        .header("User-Agent", "KakaoTalk Mobile App"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("newuser@kakao.com"))
                .andExpect(jsonPath("$.data.nickname").value(org.hamcrest.Matchers.startsWith("user_")))
                .andExpect(jsonPath("$.data.requiresNickname").value(true))
                .andExpect(cookie().exists("accessToken"));

        // === 4단계: 데이터베이스 검증 ===
        User createdUser = userRepository.findByEmail("newuser@kakao.com").orElse(null);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getNickname()).startsWith("user_");

        var socialLogin = socialLoginRepository.findByProviderAndProviderId(ProviderType.Kakao, "987654321");
        assertThat(socialLogin).isPresent();
        assertThat(socialLogin.get().getUser().getUserId()).isEqualTo(createdUser.getUserId());

        // === 5단계: Mock 서버 요청 검증 ===
        RecordedRequest tokenRequest = mockKakaoServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(tokenRequest).isNotNull();
        assertThat(tokenRequest.getPath()).isEqualTo("/oauth/token");
        assertThat(tokenRequest.getBody().readUtf8()).contains("code=mock-kakao-authorization-code");

        RecordedRequest userInfoRequest = mockKakaoServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(userInfoRequest).isNotNull();
        assertThat(userInfoRequest.getPath()).isEqualTo("/v2/user/me");
        assertThat(userInfoRequest.getHeader("Authorization")).isEqualTo("Bearer mock-kakao-access-token");
    }

    // === 예외 상황 전체 플로우 테스트 ===

    @Test
    @Order(4)
    @DisplayName("OAuth2 전체 플로우 - 토큰 획득 실패 시나리오")
    @Transactional
    void oAuth2FullFlow_TokenError_Scenario() throws Exception {
        // === 1단계: 인증 URL 생성 (성공) ===
        MvcResult authUrlResult = mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = authUrlResult.getResponse().getContentAsString();
        AuthUrlResponse authUrlResponse = objectMapper.readValue(
            objectMapper.readTree(responseContent).get("data").toString(),
            AuthUrlResponse.class
        );
        String state = authUrlResponse.getState();

        // === 2단계: Mock 서버에서 토큰 오류 응답 설정 ===
        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(400)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "error": "invalid_grant",
                    "error_description": "Invalid authorization code"
                }
                """));

        // === 3단계: OAuth2 콜백 처리 (실패) ===
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .code("invalid-authorization-code")
                .state(state)
                .build();

        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("OAUTH2_TOKEN_ERROR"))
                .andExpect(jsonPath("$.message").value("OAuth2 액세스 토큰 획득에 실패했습니다."));

        // === 4단계: 데이터베이스에 사용자가 생성되지 않았는지 확인 ===
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(socialLoginRepository.count()).isEqualTo(0);

        // === 5단계: Mock 서버 요청 검증 ===
        RecordedRequest tokenRequest = mockGoogleServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(tokenRequest).isNotNull();
        assertThat(tokenRequest.getPath()).isEqualTo("/oauth/token");
    }

    @Test
    @Order(5)
    @DisplayName("OAuth2 전체 플로우 - 사용자 정보 획득 실패 시나리오")
    @Transactional
    void oAuth2FullFlow_UserInfoError_Scenario() throws Exception {
        // === 1단계: 인증 URL 생성 ===
        MvcResult authUrlResult = mockMvc.perform(get("/api/auth/oauth2/kakao/url"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = authUrlResult.getResponse().getContentAsString();
        AuthUrlResponse authUrlResponse = objectMapper.readValue(
            objectMapper.readTree(responseContent).get("data").toString(),
            AuthUrlResponse.class
        );
        String state = authUrlResponse.getState();

        // === 2단계: Mock 서버 응답 설정 (토큰은 성공, 사용자 정보는 실패) ===
        mockKakaoServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "access_token": "valid-access-token",
                    "token_type": "bearer",
                    "expires_in": 21599
                }
                """));

        mockKakaoServer.enqueue(new MockResponse()
            .setResponseCode(401)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "msg": "this access token does not exist",
                    "code": -401
                }
                """));

        // === 3단계: OAuth2 콜백 처리 (실패) ===
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .code("valid-code-but-invalid-token")
                .state(state)
                .build();

        mockMvc.perform(post("/api/auth/oauth2/kakao/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("OAUTH2_USER_INFO_ERROR"))
                .andExpect(jsonPath("$.message").value("OAuth2 사용자 정보 획득에 실패했습니다."));

        // === 4단계: 데이터베이스 검증 ===
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(socialLoginRepository.count()).isEqualTo(0);

        // === 5단계: Mock 서버 요청 검증 ===
        RecordedRequest tokenRequest = mockKakaoServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(tokenRequest).isNotNull();

        RecordedRequest userInfoRequest = mockKakaoServer.takeRequest(5, TimeUnit.SECONDS);
        assertThat(userInfoRequest).isNotNull();
        assertThat(userInfoRequest.getHeader("Authorization")).isEqualTo("Bearer valid-access-token");
    }

    @Test
    @Order(6)
    @DisplayName("OAuth2 전체 플로우 - State 불일치 시나리오")
    @Transactional
    void oAuth2FullFlow_StateMismatch_Scenario() throws Exception {
        // === 1단계: 인증 URL 생성 ===
        MvcResult authUrlResult = mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = authUrlResult.getResponse().getContentAsString();
        AuthUrlResponse authUrlResponse = objectMapper.readValue(
            objectMapper.readTree(responseContent).get("data").toString(),
            AuthUrlResponse.class
        );
        String validState = authUrlResponse.getState();

        // === 2단계: 잘못된 state로 콜백 요청 ===
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .code("valid-authorization-code")
                .state("invalid-state-parameter") // 잘못된 state
                .build();

        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("OAUTH2_STATE_MISMATCH"))
                .andExpect(jsonPath("$.message").value("OAuth2 state 파라미터가 일치하지 않습니다."));

        // === 3단계: Mock 서버에 요청이 전송되지 않았는지 확인 ===
        // State 검증 실패로 인해 토큰 요청이 전송되지 않아야 함
        RecordedRequest tokenRequest = mockGoogleServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(tokenRequest).isNull(); // 요청이 없어야 함

        // === 4단계: 데이터베이스에 변경사항이 없는지 확인 ===
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(socialLoginRepository.count()).isEqualTo(0);
    }

    @Test
    @Order(7)
    @DisplayName("OAuth2 전체 플로우 - 소셜 계정 충돌 시나리오")
    @Transactional
    void oAuth2FullFlow_SocialAccountConflict_Scenario() throws Exception {
        // === 사전 조건: 동일한 이메일로 다른 소셜 계정이 이미 존재 ===
        User existingUser = User.builder()
                .email("conflict@example.com")
                .nickname("기존 카카오 사용자")
                .build();
        existingUser = userRepository.save(existingUser);

        // 카카오 계정으로 이미 연결됨
        socialLoginRepository.save(com.sesac.solbid.domain.SocialLogin.builder()
                .user(existingUser)
                .provider(ProviderType.Kakao)
                .providerId("kakao-existing-user")
                .build());

        // === 1단계: Google 인증 URL 생성 ===
        MvcResult authUrlResult = mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = authUrlResult.getResponse().getContentAsString();
        AuthUrlResponse authUrlResponse = objectMapper.readValue(
            objectMapper.readTree(responseContent).get("data").toString(),
            AuthUrlResponse.class
        );
        String state = authUrlResponse.getState();

        // === 2단계: Mock Google 서버 응답 설정 (동일한 이메일) ===
        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "access_token": "google-access-token",
                    "token_type": "Bearer",
                    "expires_in": 3600
                }
                """));

        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "id": "google-new-user-123",
                    "email": "conflict@example.com",
                    "name": "구글 신규사용자"
                }
                """));

        // === 3단계: OAuth2 콜백 처리 (충돌 발생) ===
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .code("google-auth-code")
                .state(state)
                .build();

        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SOCIAL_ACCOUNT_CONFLICT"))
                .andExpect(jsonPath("$.message").value("이미 다른 소셜 계정으로 연결된 이메일입니다."));

        // === 4단계: 데이터베이스 상태 검증 ===
        // 기존 사용자는 그대로 유지되어야 함
        User unchangedUser = userRepository.findByEmail("conflict@example.com").orElse(null);
        assertThat(unchangedUser).isNotNull();
        assertThat(unchangedUser.getNickname()).isEqualTo("기존 카카오 사용자"); // 변경되지 않음

        // Google 소셜 로그인 정보는 생성되지 않아야 함
        var googleSocialLogin = socialLoginRepository.findByProviderAndProviderId(ProviderType.Google, "google-new-user-123");
        assertThat(googleSocialLogin).isEmpty();

        // 기존 카카오 소셜 로그인 정보는 유지되어야 함
        var kakaoSocialLogin = socialLoginRepository.findByProviderAndProviderId(ProviderType.Kakao, "kakao-existing-user");
        assertThat(kakaoSocialLogin).isPresent();
    }

    @Test
    @Order(8)
    @DisplayName("OAuth2 전체 플로우 - 네트워크 타임아웃 시나리오")
    @Transactional
    void oAuth2FullFlow_NetworkTimeout_Scenario() throws Exception {
        // === 1단계: 인증 URL 생성 ===
        MvcResult authUrlResult = mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = authUrlResult.getResponse().getContentAsString();
        AuthUrlResponse authUrlResponse = objectMapper.readValue(
            objectMapper.readTree(responseContent).get("data").toString(),
            AuthUrlResponse.class
        );
        String state = authUrlResponse.getState();

        // === 2단계: Mock 서버에서 지연 응답 설정 (타임아웃 시뮬레이션) ===
        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "access_token": "delayed-token",
                    "token_type": "Bearer"
                }
                """)
            .setBodyDelay(15, TimeUnit.SECONDS)); // 15초 지연 (타임아웃 유발)

        // === 3단계: OAuth2 콜백 처리 (타임아웃 발생) ===
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .code("timeout-test-code")
                .state(state)
                .build();

        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("OAUTH2_TOKEN_ERROR"));

        // === 4단계: 데이터베이스에 변경사항이 없는지 확인 ===
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(socialLoginRepository.count()).isEqualTo(0);
    }

    @Test
    @Order(9)
    @DisplayName("OAuth2 전체 플로우 - 필수 사용자 정보 누락 시나리오")
    @Transactional
    void oAuth2FullFlow_MissingRequiredUserInfo_Scenario() throws Exception {
        // === 1단계: 인증 URL 생성 ===
        MvcResult authUrlResult = mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = authUrlResult.getResponse().getContentAsString();
        AuthUrlResponse authUrlResponse = objectMapper.readValue(
            objectMapper.readTree(responseContent).get("data").toString(),
            AuthUrlResponse.class
        );
        String state = authUrlResponse.getState();

        // === 2단계: Mock 서버 응답 설정 (이메일 누락) ===
        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "access_token": "valid-token",
                    "token_type": "Bearer",
                    "expires_in": 3600
                }
                """));

        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "id": "google-user-no-email",
                    "name": "이메일 없는 사용자"
                }
                """)); // 이메일 필드 누락

        // === 3단계: OAuth2 콜백 처리 (실패) ===
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .code("valid-code")
                .state(state)
                .build();

        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("OAUTH2_USER_INFO_ERROR"));

        // === 4단계: 데이터베이스 검증 ===
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(socialLoginRepository.count()).isEqualTo(0);
    }

    @Test
    @Order(10)
    @DisplayName("OAuth2 전체 플로우 - 동시 요청 처리 시나리오")
    @Transactional
    void oAuth2FullFlow_ConcurrentRequests_Scenario() throws Exception {
        // === 1단계: 두 개의 인증 URL 생성 ===
        MvcResult authUrlResult1 = mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult authUrlResult2 = mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andExpect(status().isOk())
                .andReturn();

        // 각각 다른 state를 가져야 함
        String responseContent1 = authUrlResult1.getResponse().getContentAsString();
        String responseContent2 = authUrlResult2.getResponse().getContentAsString();
        
        AuthUrlResponse authUrlResponse1 = objectMapper.readValue(
            objectMapper.readTree(responseContent1).get("data").toString(),
            AuthUrlResponse.class
        );
        AuthUrlResponse authUrlResponse2 = objectMapper.readValue(
            objectMapper.readTree(responseContent2).get("data").toString(),
            AuthUrlResponse.class
        );

        String state1 = authUrlResponse1.getState();
        String state2 = authUrlResponse2.getState();

        assertThat(state1).isNotEqualTo(state2); // 서로 다른 state

        // === 2단계: 첫 번째 요청은 성공 ===
        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "access_token": "first-token",
                    "token_type": "Bearer"
                }
                """));

        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "id": "first-user",
                    "email": "first@example.com",
                    "name": "첫번째 사용자"
                }
                """));

        CallbackRequest callbackRequest1 = CallbackRequest.builder()
                .code("first-code")
                .state(state1)
                .build();

        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("first@example.com"));

        // === 3단계: 두 번째 요청은 다른 state로 실패해야 함 ===
        CallbackRequest callbackRequest2 = CallbackRequest.builder()
                .code("second-code")
                .state(state2) // 여전히 유효한 state
                .build();

        // 두 번째 사용자를 위한 Mock 응답 설정
        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "access_token": "second-token",
                    "token_type": "Bearer"
                }
                """));

        mockGoogleServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                    "id": "second-user",
                    "email": "second@example.com",
                    "name": "두번째 사용자"
                }
                """));

        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callbackRequest2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("second@example.com"));

        // === 4단계: 두 사용자 모두 생성되었는지 확인 ===
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(socialLoginRepository.count()).isEqualTo(2);

        User firstUser = userRepository.findByEmail("first@example.com").orElse(null);
        User secondUser = userRepository.findByEmail("second@example.com").orElse(null);
        
        assertThat(firstUser).isNotNull();
        assertThat(secondUser).isNotNull();
        assertThat(firstUser.getUserId()).isNotEqualTo(secondUser.getUserId());
    }
}

