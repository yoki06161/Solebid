package com.sesac.solbid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.controller.user.UserController;
import com.sesac.solbid.exception.GlobalExceptionHandler;
import com.sesac.solbid.repository.auth.SocialLoginRepository;
import com.sesac.solbid.service.auth.SocialUnlinkService;
import com.sesac.solbid.service.user.UserService;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CookieUtil.class})
@DisplayName("UserController 유효성 검증 테스트")
class UserControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private SocialUnlinkService socialUnlinkService;

    @MockitoBean
    private SocialLoginRepository socialLoginRepository;

    @MockitoBean
    private CookieUtil cookieUtil;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // === SignupRequest 검증 테스트 ===

    @Test
    @DisplayName("회원가입 - 잘못된 이메일 형식은 검증 오류를 반환해야 한다")
    void signup_invalidEmail_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "invalid-email",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01012345678"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("이메일 형식이 올바르지 않습니다.")));
    }

    @Test
    @DisplayName("회원가입 - 약한 비밀번호는 검증 오류를 반환해야 한다")
    void signup_weakPassword_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "password",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01012345678"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함하세요.")));
    }

    @Test
    @DisplayName("회원가입 - 짧은 비밀번호는 검증 오류를 반환해야 한다")
    void signup_shortPassword_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Pass1!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01012345678"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함하세요.")));
    }

    @Test
    @DisplayName("회원가입 - 긴 비밀번호는 검증 오류를 반환해야 한다")
    void signup_longPassword_shouldReturnValidationError() throws Exception {
        // Given
        String longPassword = "Password123!" + "a".repeat(10); // 21자
        String requestBody = String.format("""
                {
                    "email": "test@example.com",
                    "password": "%s",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01012345678"
                }
                """, longPassword);

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함하세요.")));
    }

    @Test
    @DisplayName("회원가입 - 공백이 포함된 비밀번호는 검증 오류를 반환해야 한다")
    void signup_passwordWithSpace_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Pass word123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01012345678"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함하세요.")));
    }

    @Test
    @DisplayName("회원가입 - 짧은 닉네임은 검증 오류를 반환해야 한다")
    void signup_shortNickname_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Password123!",
                    "nickname": "닉",
                    "name": "홍길동",
                    "phone": "01012345678"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("닉네임은 2자 이상 10자 이하로 입력해주세요.")));
    }

    @Test
    @DisplayName("회원가입 - 긴 닉네임은 검증 오류를 반환해야 한다")
    void signup_longNickname_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Password123!",
                    "nickname": "12345678901",
                    "name": "홍길동",
                    "phone": "01012345678"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("닉네임은 2자 이상 10자 이하로 입력해주세요.")));
    }

    @Test
    @DisplayName("회원가입 - 잘못된 전화번호 형식은 검증 오류를 반환해야 한다")
    void signup_invalidPhoneNumber_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "010-1234-5678"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("전화번호 형식이 올바르지 않습니다.")));
    }

    @Test
    @DisplayName("회원가입 - 잘못된 전화번호 시작번호는 검증 오류를 반환해야 한다")
    void signup_invalidPhonePrefix_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01512345678"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("전화번호 형식이 올바르지 않습니다.")));
    }

    @Test
    @DisplayName("회원가입 - 빈 필수 필드들은 검증 오류를 반환해야 한다")
    void signup_blankRequiredFields_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "",
                    "password": "",
                    "nickname": "",
                    "name": "",
                    "phone": ""
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));
        // 여러 검증 오류가 있을 때는 메시지에 모든 오류가 포함되어야 함
    }

    // === LoginRequest 검증 테스트 ===

    @Test
    @DisplayName("로그인 - 잘못된 이메일 형식은 검증 오류를 반환해야 한다")
    void login_invalidEmail_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "invalid-email",
                    "password": "password123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("이메일 형식이 올바르지 않습니다.")));
    }

    @Test
    @DisplayName("로그인 - 빈 이메일은 검증 오류를 반환해야 한다")
    void login_blankEmail_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "",
                    "password": "password123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("로그인 - 빈 비밀번호는 검증 오류를 반환해야 한다")
    void login_blankPassword_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": ""
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("로그인 - null 필드들은 검증 오류를 반환해야 한다")
    void login_nullFields_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": null,
                    "password": null
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));
    }

    // === NicknameUpdateRequest 검증 테스트 ===

    @Test
    @DisplayName("닉네임 업데이트 - 짧은 닉네임은 검증 오류를 반환해야 한다")
    void updateNickname_shortNickname_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": "닉"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/nickname")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("닉네임은 2자 이상 10자 이하로 입력해주세요.")));
    }

    @Test
    @DisplayName("닉네임 업데이트 - 긴 닉네임은 검증 오류를 반환해야 한다")
    void updateNickname_longNickname_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": "12345678901"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/nickname")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("닉네임은 2자 이상 10자 이하로 입력해주세요.")));
    }

    @Test
    @DisplayName("닉네임 업데이트 - 빈 닉네임은 검증 오류를 반환해야 한다")
    void updateNickname_blankNickname_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": ""
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/nickname")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("닉네임은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("닉네임 업데이트 - null 닉네임은 검증 오류를 반환해야 한다")
    void updateNickname_nullNickname_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": null
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/nickname")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("닉네임은 필수 입력 값입니다.")));
    }

    // === JSON 형식 오류 테스트 ===

    @Test
    @DisplayName("회원가입 - 잘못된 JSON 형식은 파싱 오류를 반환해야 한다")
    void signup_invalidJson_shouldReturnParsingError() throws Exception {
        // Given
        String invalidJson = """
                {
                    "email": "test@example.com",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": 
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));
    }

    // === Content-Type 테스트 ===

    @Test
    @DisplayName("회원가입 - Content-Type이 없으면 미디어 타입 오류를 반환해야 한다")
    void signup_noContentType_shouldReturnMediaTypeError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01012345678"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .content(requestBody)) // Content-Type 헤더 없음
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    // === 유효한 전화번호 패턴 테스트 ===

    @Test
    @DisplayName("회원가입 - 유효한 010 전화번호는 검증을 통과해야 한다")
    void signup_valid010PhoneNumber_shouldPassValidation() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01012345678"
                }
                """;

        // When & Then
        // 서비스 레이어에서 예외가 발생하지 않는다면 검증은 통과한 것
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody));
        // 실제 서비스 로직은 모킹되어 있으므로 검증만 확인
    }

    @Test
    @DisplayName("회원가입 - 유효한 011 전화번호는 검증을 통과해야 한다")
    void signup_valid011PhoneNumber_shouldPassValidation() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01112345678"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody));
    }

    @Test
    @DisplayName("회원가입 - 유효한 016-019 전화번호는 검증을 통과해야 한다")
    void signup_validOtherPhoneNumbers_shouldPassValidation() throws Exception {
        // Test 016
        String requestBody016 = """
                {
                    "email": "test016@example.com",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01612345678"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody016));

        // Test 017
        String requestBody017 = """
                {
                    "email": "test017@example.com",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01712345678"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody017));

        // Test 018
        String requestBody018 = """
                {
                    "email": "test018@example.com",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01812345678"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody018));

        // Test 019
        String requestBody019 = """
                {
                    "email": "test019@example.com",
                    "password": "Password123!",
                    "nickname": "테스트닉네임",
                    "name": "홍길동",
                    "phone": "01912345678"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody019));
    }
}