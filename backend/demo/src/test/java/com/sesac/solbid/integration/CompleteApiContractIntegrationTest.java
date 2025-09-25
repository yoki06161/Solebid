package com.sesac.solbid.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.auth.request.*;
import com.sesac.solbid.dto.user.request.*;
import com.sesac.solbid.repository.user.UserRepository;
import com.sesac.solbid.service.user.UserService;
import com.sesac.solbid.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 전체 API 계약 유지 확인을 위한 통합 테스트
 * 
 * 테스트 목적:
 * - 모든 REST API 엔드포인트에 대한 통합 테스트 실행
 * - 클라이언트 관점에서 기존과 동일한 API 동작 확인
 * - 요청/응답 형식의 하위 호환성 검증
 * - Record DTO 변환 후 API 계약 완전성 보장
 *
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class CompleteApiContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Password123!",
                "testuser",
                "Test User",
                "01012345678"
        );
        testUser = userService.signup(signupRequest);
        accessToken = jwtUtil.generateToken(testUser.getEmail());
    }

    // ========== Auth Controller 엔드포인트 테스트 ==========

    @Test
    @DisplayName("POST /api/auth/logout - 로그아웃 API 계약 유지")
    void logout_maintainsApiContract() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /api/auth/oauth2/{provider}/url - OAuth2 URL 생성 API 계약 유지")
    void generateAuthUrl_maintainsApiContract() throws Exception {
        mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OAuth2 인증 URL이 생성되었습니다."))
                .andExpect(jsonPath("$.data.authUrl").exists())
                .andExpect(jsonPath("$.data.state").exists());
    }

    @Test
    @DisplayName("POST /api/auth/oauth2/{provider}/callback - OAuth2 콜백 요청 형식 유지")
    void handleCallback_maintainsRequestFormat() throws Exception {
        // given
        CallbackRequest callbackRequest = new CallbackRequest("test-code", "test-state");
        String requestJson = objectMapper.writeValueAsString(callbackRequest);

        // when & then - 요청 형식이 올바르게 처리되는지 확인 (실제 OAuth2 처리는 실패하지만 요청 형식은 유지)
        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest()) // OAuth2 처리 실패는 예상됨
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/auth/password/request-reset - 비밀번호 재설정 요청 API 계약 유지")
    void requestPasswordReset_maintainsApiContract() throws Exception {
        // given
        PasswordResetRequest request = new PasswordResetRequest("test@example.com");
        String requestJson = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호 재설정 인증번호를 이메일로 발송했습니다."))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("POST /api/auth/password/verify-otp - OTP 검증 요청 형식 유지")
    void verifyPasswordResetOtp_maintainsRequestFormat() throws Exception {
        // given
        PasswordResetOtpVerifyRequest request = new PasswordResetOtpVerifyRequest("test@example.com", "123456");
        String requestJson = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/auth/password/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest()) // OTP 불일치는 예상됨
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/auth/password/verify-and-reset - 비밀번호 재설정 요청 형식 유지")
    void verifyOtpAndResetPassword_maintainsRequestFormat() throws Exception {
        // given
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest("test@example.com", "123456", "NewPassword123!");
        String requestJson = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest()) // OTP 불일치는 예상됨
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/auth/password/resend-otp - OTP 재전송 요청 형식 유지")
    void resendPasswordResetOtp_maintainsRequestFormat() throws Exception {
        // given
        ResendOtpRequest request = new ResendOtpRequest("test@example.com");
        String requestJson = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/auth/password/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인증번호를 다시 발송했습니다."))
                .andExpect(jsonPath("$.data").exists());
    }

    // ========== Email Verification Controller 엔드포인트 테스트 ==========

    @Test
    @DisplayName("GET /api/auth/verify-email - 이메일 인증 토큰 검증 API 계약 유지")
    void verifyEmailWithToken_maintainsApiContract() throws Exception {
        // 유효하지 않은 토큰으로 테스트 (실제 토큰 생성은 복잡하므로 오류 응답 형식 확인)
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/auth/verify-code - 이메일 인증번호 검증 요청 형식 유지")
    void verifyEmailCode_maintainsRequestFormat() throws Exception {
        // given
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "123456");
        String requestJson = objectMapper.writeValueAsString(request);

        // when & then - 잘못된 인증번호로 오류 응답 형식 확인
        mockMvc.perform(post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/auth/verify-signup-code - 회원가입 전 이메일 인증번호 검증 요청 형식 유지")
    void verifySignupCode_maintainsRequestFormat() throws Exception {
        // given
        VerifyCodeRequest request = new VerifyCodeRequest("signup-test@example.com", "123456");
        String requestJson = objectMapper.writeValueAsString(request);

        // when & then - 잘못된 인증번호로 오류 응답 형식 확인
        mockMvc.perform(post("/api/auth/verify-signup-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/auth/send-verification - 회원가입 전 이메일 인증번호 전송 요청 형식 유지")
    void sendVerificationForSignup_maintainsRequestFormat() throws Exception {
        // given
        ResendVerificationRequest request = new ResendVerificationRequest("verification-test@example.com");
        String requestJson = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/auth/send-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인증번호를 전송했습니다."))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("POST /api/auth/resend-verification - 이메일 인증 재전송 요청 형식 유지")
    void resendVerificationEmail_maintainsRequestFormat() throws Exception {
        // given
        ResendVerificationRequest request = new ResendVerificationRequest("test@example.com");
        String requestJson = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인증번호를 재전송했습니다."))
                .andExpect(jsonPath("$.data").exists());
    }

    // ========== User Controller 엔드포인트 테스트 ==========

    @Test
    @DisplayName("POST /api/users/signup - 회원가입 API 계약 유지")
    void signup_maintainsApiContract() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "signup@example.com",
                "Password123!",
                "signupuser",
                "Signup User",
                "01087654321"
        );
        String requestJson = objectMapper.writeValueAsString(signupRequest);

        // when & then
        MvcResult result = mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.email").value("signup@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("signupuser"))
                .andExpect(jsonPath("$.data.emailVerified").value(false))
                .andExpect(jsonPath("$.data.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.nextStep").exists())
                .andExpect(jsonPath("$.message").exists())
                .andReturn();

        // JSON 구조 검증
        String responseJson = result.getResponse().getContentAsString();
        assertThat(responseJson).contains("\"email\":\"signup@example.com\"");
        assertThat(responseJson).contains("\"nickname\":\"signupuser\"");
    }

    @Test
    @DisplayName("POST /api/users/login - 로그인 API 계약 유지")
    void login_maintainsApiContract() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "Password123!");
        String requestJson = objectMapper.writeValueAsString(loginRequest);

        // when & then
        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("testuser"))
                .andExpect(jsonPath("$.data.userType").value("USER"))
                .andReturn();

        // 쿠키 설정 확인
        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).isNotEmpty();
        
        boolean hasAccessToken = false;
        boolean hasRefreshToken = false;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) hasAccessToken = true;
            if ("refreshToken".equals(cookie.getName())) hasRefreshToken = true;
        }
        assertThat(hasAccessToken).isTrue();
        assertThat(hasRefreshToken).isTrue();
    }

    @Test
    @DisplayName("GET /api/users/email/available - 이메일 중복 확인 API 계약 유지")
    void isEmailAvailable_maintainsApiContract() throws Exception {
        mockMvc.perform(get("/api/users/email/available")
                        .param("email", "available@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.email").value("available@example.com"))
                .andExpect(jsonPath("$.message").value("사용 가능한 이메일입니다."));

        mockMvc.perform(get("/api/users/email/available")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("GET /api/users/nickname/available - 닉네임 가용성 확인 API 계약 유지")
    void isNicknameAvailable_maintainsApiContract() throws Exception {
        mockMvc.perform(get("/api/users/nickname/available")
                        .param("nickname", "availablenick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(true));

        mockMvc.perform(get("/api/users/nickname/available")
                        .param("nickname", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(false));
    }

    @Test
    @DisplayName("POST /api/users/nickname - 닉네임 업데이트 API 계약 유지")
    void updateNickname_maintainsApiContract() throws Exception {
        // given
        NicknameUpdateRequest request = new NicknameUpdateRequest("newnickname");
        String requestJson = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/users/nickname")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("newnickname"))
                .andExpect(jsonPath("$.message").value("닉네임이 설정되었습니다."));
    }

    @Test
    @DisplayName("GET /api/users/me - 현재 사용자 조회 API 계약 유지")
    void getCurrentUser_maintainsApiContract() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("testuser"))
                .andExpect(jsonPath("$.data.userType").value("USER"))
                .andExpect(jsonPath("$.data.temperature").exists());
    }

    @Test
    @DisplayName("DELETE /api/users/me - 회원 탈퇴 API 계약 유지")
    void withdraw_maintainsApiContract() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원탈퇴가 완료되었습니다."))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("POST /api/users/reactivate - 계정 재활성화 API 계약 유지")
    void reactivate_maintainsApiContract() throws Exception {
        // given
        String reactivationToken = jwtUtil.generateReactivationToken("test@example.com", 600);
        String requestJson = String.format("{\"token\":\"%s\"}", reactivationToken);

        // when & then
        mockMvc.perform(post("/api/users/reactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.nickname").exists())
                .andExpect(jsonPath("$.data.userType").exists())
                .andExpect(jsonPath("$.message").value("계정이 재활성화되었습니다."));
    }

    // ========== 요청/응답 형식 하위 호환성 테스트 ==========

    @Test
    @DisplayName("모든 Record DTO 요청이 기존 JSON 형식과 호환된다")
    void allRecordDtoRequests_maintainJsonCompatibility() throws Exception {
        // SignupRequest JSON 호환성
        String signupJson = """
                {
                    "email": "json-compat@example.com",
                    "password": "Password123!",
                    "nickname": "jsonuser",
                    "name": "JSON User",
                    "phone": "01012345678"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("json-compat@example.com"));

        // LoginRequest JSON 호환성
        String loginJson = """
                {
                    "email": "json-compat@example.com",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("json-compat@example.com"));

        // NicknameUpdateRequest JSON 호환성
        String nicknameJson = """
                {
                    "nickname": "updatednick"
                }
                """;

        String userAccessToken = jwtUtil.generateToken("json-compat@example.com");
        mockMvc.perform(post("/api/users/nickname")
                        .cookie(new Cookie("accessToken", userAccessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nicknameJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("updatednick"));
    }

    @Test
    @DisplayName("모든 Record DTO 응답이 기존 JSON 구조를 유지한다")
    void allRecordDtoResponses_maintainJsonStructure() throws Exception {
        // 회원가입 응답 구조 검증
        SignupRequest signupRequest = new SignupRequest(
                "structure@example.com",
                "Password123!",
                "structureuser",
                "Structure User",
                "01012345678"
        );
        String requestJson = objectMapper.writeValueAsString(signupRequest);

        MvcResult signupResult = mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        String signupResponse = signupResult.getResponse().getContentAsString();
        
        // 필수 필드 존재 확인
        assertThat(signupResponse).contains("\"userId\":");
        assertThat(signupResponse).contains("\"email\":\"structure@example.com\"");
        assertThat(signupResponse).contains("\"nickname\":\"structureuser\"");
        assertThat(signupResponse).contains("\"emailVerified\":false");
        assertThat(signupResponse).contains("\"message\":\"회원가입이 완료되었습니다.\"");
        assertThat(signupResponse).contains("\"nextStep\":");

        // 로그인 응답 구조 검증
        LoginRequest loginRequest = new LoginRequest("structure@example.com", "Password123!");
        String loginRequestJson = objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        
        // 필수 필드 존재 확인
        assertThat(loginResponse).contains("\"userId\":");
        assertThat(loginResponse).contains("\"email\":\"structure@example.com\"");
        assertThat(loginResponse).contains("\"nickname\":\"structureuser\"");
        assertThat(loginResponse).contains("\"userType\":\"USER\"");
    }

    @Test
    @DisplayName("복잡한 시나리오에서 전체 API 계약이 유지된다")
    void complexScenario_maintainsCompleteApiContract() throws Exception {
        // 1. 회원가입
        SignupRequest signupRequest = new SignupRequest(
                "complex@example.com",
                "Password123!",
                "complexuser",
                "Complex User",
                "01012345678"
        );
        String signupJson = objectMapper.writeValueAsString(signupRequest);

        MvcResult signupResult = mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        // 2. 이메일 중복 확인 (이제 사용 불가능해야 함)
        mockMvc.perform(get("/api/users/email/available")
                        .param("email", "complex@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(false));

        // 3. 로그인
        LoginRequest loginRequest = new LoginRequest("complex@example.com", "Password123!");
        String loginJson = objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        // 4. 쿠키에서 액세스 토큰 추출
        Cookie[] cookies = loginResult.getResponse().getCookies();
        String userAccessToken = null;
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                userAccessToken = cookie.getValue();
                break;
            }
        }
        assertThat(userAccessToken).isNotNull();

        // 5. 현재 사용자 정보 조회
        mockMvc.perform(get("/api/users/me")
                        .cookie(new Cookie("accessToken", userAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("complex@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("complexuser"));

        // 6. 닉네임 업데이트
        NicknameUpdateRequest nicknameRequest = new NicknameUpdateRequest("updatedcomplex");
        String nicknameJson = objectMapper.writeValueAsString(nicknameRequest);

        mockMvc.perform(post("/api/users/nickname")
                        .cookie(new Cookie("accessToken", userAccessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nicknameJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("updatedcomplex"));

        // 7. 업데이트된 정보 확인
        mockMvc.perform(get("/api/users/me")
                        .cookie(new Cookie("accessToken", userAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("updatedcomplex"));

        // 8. 로그아웃
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("유효성 검증 오류 응답 형식이 유지된다")
    void validationErrorResponses_maintainFormat() throws Exception {
        // 잘못된 이메일 형식으로 회원가입 시도
        String invalidSignupJson = """
                {
                    "email": "invalid-email",
                    "password": "weak",
                    "nickname": "",
                    "name": "",
                    "phone": "invalid"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidSignupJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());

        // 잘못된 로그인 요청
        String invalidLoginJson = """
                {
                    "email": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLoginJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists());
    }
}