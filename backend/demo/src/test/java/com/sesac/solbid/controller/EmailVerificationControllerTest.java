package com.sesac.solbid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.controller.auth.EmailVerificationController;
import com.sesac.solbid.dto.auth.request.ResendVerificationRequest;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.EmailVerificationException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.exception.GlobalExceptionHandler;
import com.sesac.solbid.security.CustomUserDetailsService;
import com.sesac.solbid.service.user.EmailVerificationService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmailVerificationController.class)
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class})
@DisplayName("EmailVerificationController 테스트")
class EmailVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_Success() throws Exception {
        // Given
        String token = "valid-token-123";
        String email = "test@example.com";
        when(emailVerificationService.verifyEmail(token)).thenReturn(email);

        // When & Then
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이메일 인증이 완료되었습니다."))
                .andExpect(jsonPath("$.data.message").value("이메일 인증이 완료되었습니다."));

        verify(emailVerificationService).verifyEmail(token);
    }

    @Test
    @DisplayName("이메일 인증 실패 - 유효하지 않은 토큰")
    void verifyEmail_InvalidToken() throws Exception {
        // Given
        String token = "invalid-token";
        when(emailVerificationService.verifyEmail(token))
                .thenThrow(new EmailVerificationException(ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID, "test@example.com"));

        // When & Then
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("EMAIL_VERIFICATION_TOKEN_INVALID"));

        verify(emailVerificationService).verifyEmail(token);
    }

    @Test
    @DisplayName("이메일 인증 실패 - 만료된 토큰")
    void verifyEmail_ExpiredToken() throws Exception {
        // Given
        String token = "expired-token";
        when(emailVerificationService.verifyEmail(token))
                .thenThrow(new EmailVerificationException(ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED, "test@example.com"));

        // When & Then
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("EMAIL_VERIFICATION_TOKEN_EXPIRED"));

        verify(emailVerificationService).verifyEmail(token);
    }

    @Test
    @DisplayName("이메일 인증 실패 - 사용자 없음")
    void verifyEmail_UserNotFound() throws Exception {
        // Given
        String token = "valid-token";
        when(emailVerificationService.verifyEmail(token))
                .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));

        verify(emailVerificationService).verifyEmail(token);
    }

    @Test
    @DisplayName("이메일 인증 재전송 성공")
    void resendVerification_Success() throws Exception {
        // Given
        String email = "test@example.com";
        ResendVerificationRequest request = new ResendVerificationRequest(email);
        
        doNothing().when(emailVerificationService).resendVerificationEmail(email);

        // When & Then
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("인증 이메일을 재전송했습니다."));

        verify(emailVerificationService).resendVerificationEmail(email);
    }

    @Test
    @DisplayName("이메일 인증 재전송 실패 - 이미 인증된 이메일")
    void resendVerification_AlreadyVerified() throws Exception {
        // Given
        String email = "test@example.com";
        doThrow(new EmailVerificationException(ErrorCode.EMAIL_ALREADY_VERIFIED, email))
                .when(emailVerificationService).resendVerificationEmail(email);

        // When & Then
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_VERIFIED"));

        verify(emailVerificationService).resendVerificationEmail(email);
    }

    @Test
    @DisplayName("이메일 인증 재전송 실패 - 재전송 횟수 초과")
    void resendVerification_LimitExceeded() throws Exception {
        // Given
        String email = "test@example.com";
        doThrow(new EmailVerificationException(ErrorCode.EMAIL_VERIFICATION_RESEND_LIMIT_EXCEEDED, email))
                .when(emailVerificationService).resendVerificationEmail(email);

        // When & Then
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("EMAIL_VERIFICATION_RESEND_LIMIT_EXCEEDED"));

        verify(emailVerificationService).resendVerificationEmail(email);
    }

    @Test
    @DisplayName("이메일 인증 재전송 실패 - 재전송 간격 제한")
    void resendVerification_TooFrequent() throws Exception {
        // Given
        String email = "test@example.com";
        doThrow(new EmailVerificationException(ErrorCode.EMAIL_VERIFICATION_RESEND_TOO_FREQUENT, email))
                .when(emailVerificationService).resendVerificationEmail(email);

        // When & Then
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("EMAIL_VERIFICATION_RESEND_TOO_FREQUENT"));

        verify(emailVerificationService).resendVerificationEmail(email);
    }

    @Test
    @DisplayName("이메일 인증 재전송 실패 - 잘못된 이메일 형식")
    void resendVerification_InvalidEmailFormat() throws Exception {
        // Given
        String invalidEmail = "invalid-email";

        // When & Then
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + invalidEmail + "\"}"))
                .andExpect(status().isBadRequest());

        verify(emailVerificationService, never()).resendVerificationEmail(anyString());
    }

    @Test
    @DisplayName("이메일 인증 재전송 실패 - 빈 이메일")
    void resendVerification_EmptyEmail() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(emailVerificationService, never()).resendVerificationEmail(anyString());
    }

    @Test
    @DisplayName("이메일 인증 재전송 실패 - 사용자 없음")
    void resendVerification_UserNotFound() throws Exception {
        // Given
        String email = "nonexistent@example.com";
        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND))
                .when(emailVerificationService).resendVerificationEmail(email);

        // When & Then
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));

        verify(emailVerificationService).resendVerificationEmail(email);
    }

    @Test
    @DisplayName("서버 내부 오류 처리 - 이메일 인증")
    void verifyEmail_InternalServerError() throws Exception {
        // Given
        String token = "valid-token";
        when(emailVerificationService.verifyEmail(token))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));

        verify(emailVerificationService).verifyEmail(token);
    }

    @Test
    @DisplayName("서버 내부 오류 처리 - 이메일 재전송")
    void resendVerification_InternalServerError() throws Exception {
        // Given
        String email = "test@example.com";
        doThrow(new RuntimeException("Unexpected error"))
                .when(emailVerificationService).resendVerificationEmail(email);

        // When & Then
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));

        verify(emailVerificationService).resendVerificationEmail(email);
    }
}