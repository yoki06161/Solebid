package com.sesac.solbid.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.auth.request.*;
import com.sesac.solbid.dto.user.request.*;
import com.sesac.solbid.repository.UserRepository;
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
 * API 계약 검증을 위한 통합 테스트
 *
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ApiContractVerificationTest {

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

    @Test
    @DisplayName("회원가입 API - Record DTO 요청/응답 형식 유지")
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
        
        // 데이터베이스 저장 확인
        User savedUser = userRepository.findByEmail("signup@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("signup@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("signupuser");
    }

    @Test
    @DisplayName("로그인 API - Record DTO 요청/응답 형식 유지")
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
    @DisplayName("닉네임 업데이트 API - Record DTO 요청/응답 형식 유지")
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
    @DisplayName("OAuth2 URL 생성 API - Record DTO 응답 형식 유지")
    void generateAuthUrl_maintainsApiContract() throws Exception {
        mockMvc.perform(get("/api/auth/oauth2/google/url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OAuth2 인증 URL이 생성되었습니다."))
                .andExpect(jsonPath("$.data.authUrl").exists())
                .andExpect(jsonPath("$.data.state").exists());
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 API - Record DTO 요청 형식 유지")
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
    @DisplayName("OAuth2 콜백 요청 형식 유지")
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
    @DisplayName("이메일 중복 확인 API - 응답 형식 유지")
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
    @DisplayName("닉네임 가용성 확인 API - 응답 형식 유지")
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
    @DisplayName("현재 사용자 조회 API - 응답 형식 유지")
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
    @DisplayName("JSON 직렬화/역직렬화 호환성 유지")
    void jsonCompatibility_maintained() throws Exception {
        // 회원가입 JSON 호환성
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

        // 로그인 JSON 호환성
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
    }

    @Test
    @DisplayName("유효성 검증 오류 응답 형식 유지")
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

    @Test
    @DisplayName("복합 시나리오 - 전체 API 계약 유지")
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

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

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
    }

    @Test
    @DisplayName("Record DTO 불변성이 API 처리에서 보장됨")
    void recordImmutability_guaranteedInApiProcessing() throws Exception {
        // given
        SignupRequest originalRequest = new SignupRequest(
                "immutable@example.com",
                "Password123!",
                "immutableuser",
                "Immutable User",
                "01044444444"
        );
        String requestJson = objectMapper.writeValueAsString(originalRequest);

        // when
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("immutable@example.com"));

        // then - 원본 Record는 변경되지 않음
        assertThat(originalRequest.email()).isEqualTo("immutable@example.com");
        assertThat(originalRequest.nickname()).isEqualTo("immutableuser");
        
        // 데이터베이스에는 정상적으로 저장됨
        User savedUser = userRepository.findByEmail("immutable@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("immutable@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("immutableuser");
    }
}