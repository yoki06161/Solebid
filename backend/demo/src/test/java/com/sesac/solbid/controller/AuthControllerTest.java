package com.sesac.solbid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.controller.auth.AuthController;
import com.sesac.solbid.domain.enums.UserType;
import com.sesac.solbid.dto.user.response.LoginResponse;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.exception.GlobalExceptionHandler;
import com.sesac.solbid.exception.OAuth2Exception;
import com.sesac.solbid.service.auth.OAuth2Service;
import com.sesac.solbid.service.user.PasswordResetService;
import com.sesac.solbid.dto.auth.response.AuthUrlResponse;
import com.sesac.solbid.dto.auth.request.CallbackRequest;
import com.sesac.solbid.dto.auth.request.PasswordResetRequest;
import com.sesac.solbid.dto.auth.request.PasswordResetVerifyRequest;
import com.sesac.solbid.dto.auth.request.ResendOtpRequest;
import com.sesac.solbid.exception.PasswordResetException;
import com.sesac.solbid.util.CookieUtil;
import com.sesac.solbid.util.JwtUtil;
import com.sesac.solbid.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 통합 테스트
 * @WebMvcTest를 사용하여 웹 레이어를 테스트합니다.
 * SecurityConfig을 비활성화하고, 웹 애플리케이션 타입을 Servlet으로 강제하여 테스트합니다.
 */
@WebMvcTest(controllers = AuthController.class)
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CookieUtil.class})
@DisplayName("AuthController 통합 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OAuth2Service oAuth2Service;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // === 성공 시나리오 테스트 ===

    @Test
    @DisplayName("OAuth2 인증 URL 생성 성공 - Google")
    void generateAuthUrl_Success_Google() throws Exception {
        // Given
        String provider = "google";
        AuthUrlResponse mockResponse = new AuthUrlResponse(
                "https://accounts.google.com/oauth/authorize?client_id=test&state=test-state",
                "test-state-12345",
                provider
        );

        when(oAuth2Service.generateAuthUrl(provider)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/auth/oauth2/{provider}/url", provider)
                        .header("User-Agent", "Mozilla/5.0 (Test Browser)")
                        .header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OAuth2 인증 URL이 생성되었습니다."))
                .andExpect(jsonPath("$.data.authUrl").value(mockResponse.authUrl()));

        verify(oAuth2Service).generateAuthUrl(provider);
    }

    @Test
    @DisplayName("OAuth2 인증 URL 생성 성공 - Kakao")
    void generateAuthUrl_Success_Kakao() throws Exception {
        // Given
        String provider = "kakao";
        AuthUrlResponse mockResponse = new AuthUrlResponse(
                "https://kauth.kakao.com/oauth/authorize?client_id=test&state=kakao-state",
                "kakao-state-67890",
                provider
        );

        when(oAuth2Service.generateAuthUrl(provider)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/auth/oauth2/{provider}/url", provider)
                        .header("User-Agent", "KakaoTalk Mobile App")
                        .header("X-Real-IP", "10.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.provider").value("kakao"));

        verify(oAuth2Service).generateAuthUrl(provider);
    }

    @Test
    @DisplayName("OAuth2 콜백 처리 성공 - Google")
    void handleCallback_Success_Google() throws Exception {
        // Given
        String provider = "google";
        CallbackRequest request = new CallbackRequest(
                "test-auth-code-12345",
                "test-state-12345"
        );

        // TTL 스텁
        when(jwtUtil.getAccessTokenValiditySeconds()).thenReturn(3600L);
        when(jwtUtil.getRefreshTokenValiditySeconds()).thenReturn(86400L);

        LoginResponse mockLoginResponse = new LoginResponse(
                1L,
                "test@gmail.com",
                "테스트사용자",
                "테스트이름",
                "010-1234-5678",
                UserType.USER,
                "jwt-access-token",
                "jwt-refresh-token"
        );

        when(oAuth2Service.processCallback(provider, request.code(), request.state()))
                .thenReturn(mockLoginResponse);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("User-Agent", "Mozilla/5.0 (Test Browser)")
                        .header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("소셜로그인이 완료되었습니다."))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@gmail.com"))
                // 쿠키 설정 검증
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().path("accessToken", "/"))
                .andExpect(cookie().path("refreshToken", "/"))
                .andExpect(cookie().maxAge("accessToken", 3600))
                .andExpect(cookie().maxAge("refreshToken", 86400))
                .andReturn();

        // 응답에서 토큰이 제외되었는지 확인
        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).doesNotContain("jwt-access-token");
        assertThat(responseContent).doesNotContain("jwt-refresh-token");
        assertThat(responseContent).doesNotContain("accessToken");
        assertThat(responseContent).doesNotContain("refreshToken");


        verify(oAuth2Service).processCallback(provider, request.code(), request.state());
    }

    @Test
    @DisplayName("OAuth2 콜백 처리 성공 - Kakao")
    void handleCallback_Success_Kakao() throws Exception {
        // Given
        String provider = "kakao";
        CallbackRequest request = new CallbackRequest(
                "kakao-auth-code-67890",
                "kakao-state-67890"
        );

        when(jwtUtil.getAccessTokenValiditySeconds()).thenReturn(3600L);
        when(jwtUtil.getRefreshTokenValiditySeconds()).thenReturn(86400L);

        LoginResponse mockLoginResponse = new LoginResponse(
                2L,
                "user@kakao.com",
                "카카오사용자",
                "카카오이름",
                "010-9876-5432",
                UserType.USER,
                "kakao-jwt-access",
                "kakao-jwt-refresh"
        );

        when(oAuth2Service.processCallback(provider, request.code(), request.state()))
                .thenReturn(mockLoginResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("User-Agent", "KakaoTalk Mobile App")
                        .header("X-Real-IP", "10.0.0.2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(2L))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"));

        verify(oAuth2Service).processCallback(provider, request.code(), request.state());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."))
                // 쿠키 삭제 검증
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().value("accessToken", ""))
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    // === 에러 시나리오 테스트 ===

    @Test
    @DisplayName("인증 URL 생성 실패 - 지원하지 않는 Provider")
    void generateAuthUrl_Fail_InvalidProvider() throws Exception {
        // Given
        String invalidProvider = "facebook";

        when(oAuth2Service.generateAuthUrl(invalidProvider))
                .thenThrow(new OAuth2Exception(ErrorCode.INVALID_OAUTH2_PROVIDER));

        // When & Then
        mockMvc.perform(get("/api/auth/oauth2/{provider}/url", invalidProvider))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_OAUTH2_PROVIDER"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 OAuth2 제공자입니다."));

        verify(oAuth2Service).generateAuthUrl(invalidProvider);
    }

    @Test
    @DisplayName("인증 URL 생성 실패 - 서버 내부 오류")
    void generateAuthUrl_Fail_InternalServerError() throws Exception {
        // Given
        String provider = "google";

        when(oAuth2Service.generateAuthUrl(provider))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(get("/api/auth/oauth2/{provider}/url", provider))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));

        verify(oAuth2Service).generateAuthUrl(provider);
    }

    @Test
    @DisplayName("콜백 처리 실패 - State 불일치")
    void handleCallback_Fail_StateMismatch() throws Exception {
        // Given
        String provider = "google";
        CallbackRequest request = new CallbackRequest(
                "test-auth-code",
                "invalid-state"
        );

        when(oAuth2Service.processCallback(provider, request.code(), request.state()))
                .thenThrow(new OAuth2Exception(ErrorCode.OAUTH2_STATE_MISMATCH));

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("OAUTH2_STATE_MISMATCH"))
                .andExpect(jsonPath("$.message").value("OAuth2 state 파라미터가 일치하지 않습니다."));

        verify(oAuth2Service).processCallback(provider, request.code(), request.state());
    }

    @Test
    @DisplayName("콜백 처리 실패 - 토큰 획득 오류")
    void handleCallback_Fail_TokenError() throws Exception {
        // Given
        String provider = "google";
        CallbackRequest request = new CallbackRequest(
                "invalid-auth-code",
                "valid-state"
        );

        when(oAuth2Service.processCallback(provider, request.code(), request.state()))
                .thenThrow(new OAuth2Exception(ErrorCode.OAUTH2_TOKEN_ERROR));

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("OAUTH2_TOKEN_ERROR"))
                .andExpect(jsonPath("$.message").value("OAuth2 액세스 토큰 획득에 실패했습니다."));

        verify(oAuth2Service).processCallback(provider, request.code(), request.state());
    }

    @Test
    @DisplayName("콜백 처리 실패 - 사용자 정보 획득 오류")
    void handleCallback_Fail_UserInfoError() throws Exception {
        // Given
        String provider = "kakao";
        CallbackRequest request = new CallbackRequest(
                "valid-auth-code",
                "valid-state"
        );

        when(oAuth2Service.processCallback(provider, request.code(), request.state()))
                .thenThrow(new OAuth2Exception(ErrorCode.OAUTH2_USER_INFO_ERROR));

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("OAUTH2_USER_INFO_ERROR"))
                .andExpect(jsonPath("$.message").value("OAuth2 사용자 정보 획득에 실패했습니다."));

        verify(oAuth2Service).processCallback(provider, request.code(), request.state());
    }

    @Test
    @DisplayName("콜백 처리 실패 - 소셜 계정 충돌")
    void handleCallback_Fail_SocialAccountConflict() throws Exception {
        // Given
        String provider = "google";
        CallbackRequest request = new CallbackRequest(
                "valid-auth-code",
                "valid-state"
        );

        when(oAuth2Service.processCallback(provider, request.code(), request.state()))
                .thenThrow(new OAuth2Exception(ErrorCode.SOCIAL_ACCOUNT_CONFLICT));

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SOCIAL_ACCOUNT_CONFLICT"))
                .andExpect(jsonPath("$.message").value("이미 다른 소셜 계정으로 연결된 이메일입니다."));

        verify(oAuth2Service).processCallback(provider, request.code(), request.state());
    }

    @Test
    @DisplayName("콜백 처리 실패 - 서버 내부 오류")
    void handleCallback_Fail_InternalServerError() throws Exception {
        // Given
        String provider = "google";
        CallbackRequest request = new CallbackRequest(
                "valid-auth-code",
                "valid-state"
        );

        when(oAuth2Service.processCallback(provider, request.code(), request.state()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));

        verify(oAuth2Service).processCallback(provider, request.code(), request.state());
    }

    // === 요청 검증 테스트 ===

    @Test
    @DisplayName("콜백 처리 실패 - 필수 파라미터 누락 (code)")
    void handleCallback_Fail_MissingCode() throws Exception {
        // Given
        String provider = "google";
        CallbackRequest request = new CallbackRequest(
                "", // 빈 문자열
                "valid-state"
        );

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("인증 코드는 필수입니다.")));


        verify(oAuth2Service, never()).processCallback(any(), any(), any());
    }

    @Test
    @DisplayName("콜백 처리 실패 - 필수 파라미터 누락 (state)")
    void handleCallback_Fail_MissingState() throws Exception {
        // Given
        String provider = "google";
        CallbackRequest request = new CallbackRequest(
                "valid-auth-code",
                "" // 빈 문자열
        );

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("State 파라미터는 필수입니다.")));

        verify(oAuth2Service, never()).processCallback(any(), any(), any());
    }

    @Test
    @DisplayName("콜백 처리 실패 - 잘못된 JSON 형식")
    void handleCallback_Fail_InvalidJson() throws Exception {
        // Given
        String provider = "google";
        String invalidJson = "{\"code\":\"test\",\"state\":}"; // 잘못된 JSON

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));


        verify(oAuth2Service, never()).processCallback(any(), any(), any());
    }

    @Test
    @DisplayName("콜백 처리 실패 - Content-Type 누락")
    void handleCallback_Fail_MissingContentType() throws Exception {
        // Given
        String provider = "google";
        CallbackRequest request = new CallbackRequest(
                "valid-auth-code",
                "valid-state"
        );

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        // Content-Type 헤더 누락
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_MEDIA_TYPE"));


        verify(oAuth2Service, never()).processCallback(any(), any(), any());
    }

    // === 클라이언트 환경 테스트 ===

    @Test
    @DisplayName("인증 URL 생성 - 다양한 클라이언트 IP 헤더 처리")
    void generateAuthUrl_VariousClientIpHeaders() throws Exception {
        // Given
        String provider = "google";
        AuthUrlResponse mockResponse = new AuthUrlResponse(
                "https://accounts.google.com/oauth/authorize",
                "test-state",
                provider
        );

        when(oAuth2Service.generateAuthUrl(provider)).thenReturn(mockResponse);

        // When & Then - X-Forwarded-For 헤더
        mockMvc.perform(get("/api/auth/oauth2/{provider}/url", provider)
                        .header("X-Forwarded-For", "203.0.113.1, 198.51.100.1"))
                .andExpect(status().isOk());

        // When & Then - X-Real-IP 헤더
        mockMvc.perform(get("/api/auth/oauth2/{provider}/url", provider)
                        .header("X-Real-IP", "203.0.113.2"))
                .andExpect(status().isOk());

        // When & Then - 헤더 없음 (RemoteAddr 사용)
        mockMvc.perform(get("/api/auth/oauth2/{provider}/url", provider))
                .andExpect(status().isOk());

        verify(oAuth2Service, times(3)).generateAuthUrl(provider);
    }

    @Test
    @DisplayName("인증 URL 생성 - User-Agent 없는 요청")
    void generateAuthUrl_NoUserAgent() throws Exception {
        // Given
        String provider = "google";
        AuthUrlResponse mockResponse = new AuthUrlResponse(
                "https://accounts.google.com/oauth/authorize",
                "test-state",
                provider
        );

        when(oAuth2Service.generateAuthUrl(provider)).thenReturn(mockResponse);

        // When & Then - User-Agent 헤더 없음
        mockMvc.perform(get("/api/auth/oauth2/{provider}/url", provider))
                .andExpect(status().isOk());

        verify(oAuth2Service).generateAuthUrl(provider);
    }

    @Test
    @DisplayName("콜백 처리 - 매우 긴 인증 코드 처리")
    void handleCallback_LongAuthCode() throws Exception {
        // Given
        String provider = "google";
        String longAuthCode = "a".repeat(1000); // 1000자 인증 코드
        CallbackRequest request = new CallbackRequest(
                longAuthCode,
                "valid-state"
        );

        when(jwtUtil.getAccessTokenValiditySeconds()).thenReturn(3600L);
        when(jwtUtil.getRefreshTokenValiditySeconds()).thenReturn(86400L);

        LoginResponse mockResponse = new LoginResponse(
                1L,
                "test@example.com",
                "테스트",
                "테스트이름",
                "010-1111-2222",
                UserType.USER,
                "token",
                "refresh"
        );

        when(oAuth2Service.processCallback(provider, longAuthCode, "valid-state"))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(oAuth2Service).processCallback(provider, longAuthCode, "valid-state");
    }

    @Test
    @DisplayName("콜백 처리 - 특수문자가 포함된 State 처리")
    void handleCallback_SpecialCharactersInState() throws Exception {
        // Given
        String provider = "kakao";
        String specialState = "state-with-special-chars-!@#$%^&*()_+-=[]{}|;:,.<>?";
        CallbackRequest request = new CallbackRequest(
                "valid-code",
                specialState
        );

        when(jwtUtil.getAccessTokenValiditySeconds()).thenReturn(3600L);
        when(jwtUtil.getRefreshTokenValiditySeconds()).thenReturn(86400L);

        LoginResponse mockResponse = new LoginResponse(
                2L,
                "kakao@example.com",
                "카카오",
                "카카오이름",
                "010-3333-4444",
                UserType.USER,
                "token",
                "refresh"
        );

        when(oAuth2Service.processCallback(provider, "valid-code", specialState))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/{provider}/callback", provider)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(oAuth2Service).processCallback(provider, "valid-code", specialState);
    }

    @Test
    @DisplayName("로그아웃 - 이미 로그아웃된 상태에서 재요청")
    void logout_AlreadyLoggedOut() throws Exception {
        // When & Then - 여러 번 로그아웃 요청해도 성공
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // === 비밀번호 재설정 테스트 ===

    @Test
    @DisplayName("비밀번호 재설정 OTP 요청 - 성공")
    void requestPasswordReset_Success() throws Exception {
        // Given
        String email = "test@example.com";
        PasswordResetRequest request = new PasswordResetRequest(email);

        doNothing().when(passwordResetService).requestResetWithOtp(email);

        // When & Then
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}")
                        .header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호 재설정 인증번호를 이메일로 발송했습니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(passwordResetService).requestResetWithOtp(email);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 요청 - 잘못된 이메일 형식")
    void requestPasswordReset_InvalidEmailFormat() throws Exception {
        // Given
        String invalidEmail = "invalid-email";

        // When & Then
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + invalidEmail + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));

        verify(passwordResetService, never()).requestResetWithOtp(anyString());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 요청 - 빈 이메일")
    void requestPasswordReset_EmptyEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));

        verify(passwordResetService, never()).requestResetWithOtp(anyString());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 요청 - 소셜 로그인 사용자")
    void requestPasswordReset_SocialUser() throws Exception {
        // Given
        String email = "social@example.com";
        doThrow(new PasswordResetException(ErrorCode.PASSWORD_RESET_NOT_ALLOWED, email))
                .when(passwordResetService).requestResetWithOtp(email);

        // When & Then
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_RESET_NOT_ALLOWED"));

        verify(passwordResetService).requestResetWithOtp(email);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 요청 - 서버 내부 오류")
    void requestPasswordReset_InternalServerError() throws Exception {
        // Given
        String email = "test@example.com";
        doThrow(new RuntimeException("Database connection failed"))
                .when(passwordResetService).requestResetWithOtp(email);

        // When & Then
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));

        verify(passwordResetService).requestResetWithOtp(email);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 검증 및 변경 - 성공")
    void verifyOtpAndResetPassword_Success() throws Exception {
        // Given
        String email = "test@example.com";
        String otp = "123456";
        String newPassword = "newPassword123!";
        
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest(
                email,
                otp,
                newPassword
        );

        doNothing().when(passwordResetService).verifyOtpAndReset(email, otp, newPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 재설정되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(passwordResetService).verifyOtpAndReset(email, otp, newPassword);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 검증 및 변경 - 유효하지 않은 OTP")
    void verifyOtpAndResetPassword_InvalidOtp() throws Exception {
        // Given
        String email = "test@example.com";
        String invalidOtp = "999999";
        String newPassword = "newPassword123!";
        
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest(
                email,
                invalidOtp,
                newPassword
        );

        doThrow(new PasswordResetException(ErrorCode.PASSWORD_RESET_OTP_INVALID, email))
                .when(passwordResetService).verifyOtpAndReset(email, invalidOtp, newPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_RESET_OTP_INVALID"));

        verify(passwordResetService).verifyOtpAndReset(email, invalidOtp, newPassword);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 검증 및 변경 - 만료된 OTP")
    void verifyOtpAndResetPassword_ExpiredOtp() throws Exception {
        // Given
        String email = "test@example.com";
        String expiredOtp = "123456";
        String newPassword = "newPassword123!";
        
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest(
                email,
                expiredOtp,
                newPassword
        );

        doThrow(new PasswordResetException(ErrorCode.PASSWORD_RESET_OTP_EXPIRED, email))
                .when(passwordResetService).verifyOtpAndReset(email, expiredOtp, newPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_RESET_OTP_EXPIRED"));

        verify(passwordResetService).verifyOtpAndReset(email, expiredOtp, newPassword);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 검증 및 변경 - 사용자 없음")
    void verifyOtpAndResetPassword_UserNotFound() throws Exception {
        // Given
        String email = "nonexistent@example.com";
        String otp = "123456";
        String newPassword = "newPassword123!";
        
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest(
                email,
                otp,
                newPassword
        );

        doThrow(new PasswordResetException(ErrorCode.USER_NOT_FOUND, email))
                .when(passwordResetService).verifyOtpAndReset(email, otp, newPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));

        verify(passwordResetService).verifyOtpAndReset(email, otp, newPassword);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 검증 및 변경 - 기존 비밀번호와 동일")
    void verifyOtpAndResetPassword_SameAsOldPassword() throws Exception {
        // Given
        String email = "test@example.com";
        String otp = "123456";
        String samePassword = "oldPassword123!";
        
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest(
                email,
                otp,
                samePassword
        );

        doThrow(new PasswordResetException(ErrorCode.PASSWORD_RESET_SAME_AS_OLD, email))
                .when(passwordResetService).verifyOtpAndReset(email, otp, samePassword);

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_RESET_SAME_AS_OLD"));

        verify(passwordResetService).verifyOtpAndReset(email, otp, samePassword);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 검증 및 변경 - 잘못된 OTP 형식")
    void verifyOtpAndResetPassword_InvalidOtpFormat() throws Exception {
        // Given
        String email = "test@example.com";
        String invalidOtp = "12345"; // 5자리
        String newPassword = "newPassword123!";
        
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest(
                email,
                invalidOtp,
                newPassword
        );

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));

        verify(passwordResetService, never()).verifyOtpAndReset(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 검증 및 변경 - 짧은 비밀번호")
    void verifyOtpAndResetPassword_ShortPassword() throws Exception {
        // Given
        String email = "test@example.com";
        String otp = "123456";
        String shortPassword = "123"; // 3자리
        
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest(
                email,
                otp,
                shortPassword
        );

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));

        verify(passwordResetService, never()).verifyOtpAndReset(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 성공")
    void resendPasswordResetOtp_Success() throws Exception {
        // Given
        String email = "test@example.com";
        ResendOtpRequest request = new ResendOtpRequest(email);

        doNothing().when(passwordResetService).resendResetOtp(email);

        // When & Then
        mockMvc.perform(post("/api/auth/password/resend-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인증번호를 다시 발송했습니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(passwordResetService).resendResetOtp(email);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 사용자 없음")
    void resendPasswordResetOtp_UserNotFound() throws Exception {
        // Given
        String email = "nonexistent@example.com";
        ResendOtpRequest request = new ResendOtpRequest(email);

        doThrow(new PasswordResetException(ErrorCode.USER_NOT_FOUND, email))
                .when(passwordResetService).resendResetOtp(email);

        // When & Then
        mockMvc.perform(post("/api/auth/password/resend-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));

        verify(passwordResetService).resendResetOtp(email);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 재전송 횟수 초과")
    void resendPasswordResetOtp_LimitExceeded() throws Exception {
        // Given
        String email = "test@example.com";
        ResendOtpRequest request = new ResendOtpRequest(email);

        doThrow(new PasswordResetException(ErrorCode.PASSWORD_RESET_RESEND_LIMIT_EXCEEDED, email))
                .when(passwordResetService).resendResetOtp(email);

        // When & Then
        mockMvc.perform(post("/api/auth/password/resend-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_RESET_RESEND_LIMIT_EXCEEDED"));

        verify(passwordResetService).resendResetOtp(email);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 재전송 간격 제한")
    void resendPasswordResetOtp_TooFrequent() throws Exception {
        // Given
        String email = "test@example.com";
        ResendOtpRequest request = new ResendOtpRequest(email);

        doThrow(new PasswordResetException(ErrorCode.PASSWORD_RESET_RESEND_TOO_FREQUENT, email))
                .when(passwordResetService).resendResetOtp(email);

        // When & Then
        mockMvc.perform(post("/api/auth/password/resend-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_RESET_RESEND_TOO_FREQUENT"));

        verify(passwordResetService).resendResetOtp(email);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 소셜 로그인 사용자")
    void resendPasswordResetOtp_SocialUser() throws Exception {
        // Given
        String email = "social@example.com";
        ResendOtpRequest request = new ResendOtpRequest(email);

        doThrow(new PasswordResetException(ErrorCode.PASSWORD_RESET_NOT_ALLOWED, email))
                .when(passwordResetService).resendResetOtp(email);

        // When & Then
        mockMvc.perform(post("/api/auth/password/resend-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_RESET_NOT_ALLOWED"));

        verify(passwordResetService).resendResetOtp(email);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 잘못된 이메일 형식")
    void resendPasswordResetOtp_InvalidEmailFormat() throws Exception {
        // Given
        String invalidEmail = "invalid-email";
        ResendOtpRequest request = new ResendOtpRequest(invalidEmail);

        // When & Then
        mockMvc.perform(post("/api/auth/password/resend-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));

        verify(passwordResetService, never()).resendResetOtp(anyString());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 빈 이메일")
    void resendPasswordResetOtp_EmptyEmail() throws Exception {
        // Given
        ResendOtpRequest request = new ResendOtpRequest("");

        // When & Then
        mockMvc.perform(post("/api/auth/password/resend-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));

        verify(passwordResetService, never()).resendResetOtp(anyString());
    }

    @Test
    @DisplayName("비밀번호 재설정 - 다양한 클라이언트 IP 헤더 처리")
    void passwordReset_VariousClientIpHeaders() throws Exception {
        // Given
        String email = "test@example.com";
        doNothing().when(passwordResetService).requestResetWithOtp(email);

        // When & Then - X-Forwarded-For 헤더
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}")
                        .header("X-Forwarded-For", "203.0.113.1, 198.51.100.1"))
                .andExpect(status().isOk());

        // When & Then - X-Real-IP 헤더
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}")
                        .header("X-Real-IP", "203.0.113.2"))
                .andExpect(status().isOk());

        // When & Then - 헤더 없음 (RemoteAddr 사용)
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk());

        verify(passwordResetService, times(3)).requestResetWithOtp(email);
    }
}
