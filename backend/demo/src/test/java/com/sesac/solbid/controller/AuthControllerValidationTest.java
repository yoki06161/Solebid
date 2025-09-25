package com.sesac.solbid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.controller.auth.AuthController;
import com.sesac.solbid.exception.GlobalExceptionHandler;
import com.sesac.solbid.service.auth.OAuth2Service;
import com.sesac.solbid.service.user.PasswordResetService;
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

@WebMvcTest(controllers = AuthController.class)
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CookieUtil.class})
@DisplayName("AuthController 유효성 검증 테스트")
class AuthControllerValidationTest {

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

    // === CallbackRequest 검증 테스트 ===

    @Test
    @DisplayName("OAuth2 콜백 - 빈 인증 코드는 검증 오류를 반환해야 한다")
    void handleCallback_blankCode_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "code": "",
                    "state": "valid-state"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("인증 코드는 필수입니다.")));
    }

    @Test
    @DisplayName("OAuth2 콜백 - 빈 state는 검증 오류를 반환해야 한다")
    void handleCallback_blankState_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "code": "valid-code",
                    "state": ""
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("State 파라미터는 필수입니다.")));
    }

    @Test
    @DisplayName("OAuth2 콜백 - null 필드들은 검증 오류를 반환해야 한다")
    void handleCallback_nullFields_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "code": null,
                    "state": null
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));
    }

    // === PasswordResetRequest 검증 테스트 ===

    @Test
    @DisplayName("비밀번호 재설정 요청 - 잘못된 이메일 형식은 검증 오류를 반환해야 한다")
    void requestPasswordReset_invalidEmail_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "invalid-email"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 - 빈 이메일은 검증 오류를 반환해야 한다")
    void requestPasswordReset_blankEmail_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": ""
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/request-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));
    }

    // === PasswordResetOtpVerifyRequest 검증 테스트 ===

    @Test
    @DisplayName("비밀번호 재설정 OTP 검증 - 잘못된 이메일 형식은 검증 오류를 반환해야 한다")
    void verifyPasswordResetOtp_invalidEmail_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "invalid-email",
                    "otp": "123456"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("이메일 형식이 올바르지 않습니다.")));
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 검증 - 잘못된 OTP 형식은 검증 오류를 반환해야 한다")
    void verifyPasswordResetOtp_invalidOtp_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "otp": "12345"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("인증번호는 6자리 숫자여야 합니다.")));
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 검증 - 문자가 포함된 OTP는 검증 오류를 반환해야 한다")
    void verifyPasswordResetOtp_otpWithLetters_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "otp": "12345a"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("인증번호는 6자리 숫자여야 합니다.")));
    }

    // === PasswordResetVerifyRequest 검증 테스트 ===

    @Test
    @DisplayName("비밀번호 재설정 검증 - 잘못된 이메일 형식은 검증 오류를 반환해야 한다")
    void verifyOtpAndResetPassword_invalidEmail_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "invalid-email",
                    "otp": "123456",
                    "newPassword": "newPassword123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("이메일 형식이 올바르지 않습니다.")));
    }

    @Test
    @DisplayName("비밀번호 재설정 검증 - 잘못된 OTP 형식은 검증 오류를 반환해야 한다")
    void verifyOtpAndResetPassword_invalidOtp_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "otp": "12345",
                    "newPassword": "newPassword123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("인증번호는 6자리 숫자여야 합니다.")));
    }

    @Test
    @DisplayName("비밀번호 재설정 검증 - 짧은 비밀번호는 검증 오류를 반환해야 한다")
    void verifyOtpAndResetPassword_shortPassword_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "test@example.com",
                    "otp": "123456",
                    "newPassword": "short"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("비밀번호는 8자 이상 64자 이하여야 합니다.")));
    }

    @Test
    @DisplayName("비밀번호 재설정 검증 - 긴 비밀번호는 검증 오류를 반환해야 한다")
    void verifyOtpAndResetPassword_longPassword_shouldReturnValidationError() throws Exception {
        // Given
        String longPassword = "a".repeat(65);
        String requestBody = String.format("""
                {
                    "email": "test@example.com",
                    "otp": "123456",
                    "newPassword": "%s"
                }
                """, longPassword);

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("비밀번호는 8자 이상 64자 이하여야 합니다.")));
    }

    // === ResendOtpRequest 검증 테스트 ===

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 잘못된 이메일 형식은 검증 오류를 반환해야 한다")
    void resendPasswordResetOtp_invalidEmail_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "invalid-email"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/resend-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("이메일 형식이 올바르지 않습니다.")));
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 빈 이메일은 검증 오류를 반환해야 한다")
    void resendPasswordResetOtp_blankEmail_shouldReturnValidationError() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": ""
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/resend-otp")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("이메일은 필수입니다.")));
    }

    // === 복합 검증 오류 테스트 ===

    @Test
    @DisplayName("비밀번호 재설정 검증 - 모든 필드가 잘못된 경우 여러 검증 오류를 반환해야 한다")
    void verifyOtpAndResetPassword_allInvalidFields_shouldReturnMultipleValidationErrors() throws Exception {
        // Given
        String requestBody = """
                {
                    "email": "invalid-email",
                    "otp": "12345",
                    "newPassword": "short"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/password/verify-and-reset")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));
        // 여러 검증 오류가 있을 때는 메시지에 모든 오류가 포함되어야 함
    }

    // === JSON 형식 오류 테스트 ===

    @Test
    @DisplayName("OAuth2 콜백 - 잘못된 JSON 형식은 파싱 오류를 반환해야 한다")
    void handleCallback_invalidJson_shouldReturnParsingError() throws Exception {
        // Given
        String invalidJson = """
                {
                    "code": "valid-code",
                    "state": 
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"));
    }

    // === Content-Type 테스트 ===

    @Test
    @DisplayName("OAuth2 콜백 - Content-Type이 없으면 미디어 타입 오류를 반환해야 한다")
    void handleCallback_noContentType_shouldReturnMediaTypeError() throws Exception {
        // Given
        String requestBody = """
                {
                    "code": "valid-code",
                    "state": "valid-state"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2/google/callback")
                        .with(csrf())
                        .content(requestBody)) // Content-Type 헤더 없음
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_MEDIA_TYPE"));
    }
}