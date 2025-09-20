package com.sesac.solbid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.UserStatus;
import com.sesac.solbid.domain.enums.UserType;
import com.sesac.solbid.dto.user.request.SensitiveProfileUpdateRequest;
import com.sesac.solbid.dto.user.request.PasswordChangeRequest;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.exception.GlobalExceptionHandler;
import com.sesac.solbid.repository.SocialLoginRepository;
import com.sesac.solbid.security.CustomUserDetailsService;
import com.sesac.solbid.service.auth.SocialUnlinkService;
import com.sesac.solbid.service.user.UserService;
import com.sesac.solbid.util.JwtUtil;
import com.sesac.solbid.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 보안 관련 통합 테스트
 * <p>
 * 민감한 프로필 업데이트, 비밀번호 변경 등 보안이 강화된 기능들의
 * HTTP 요청/응답 처리를 테스트합니다.
 * </p>
 */
@WebMvcTest(controllers = UserController.class)
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CookieUtil.class})
@DisplayName("UserController 보안 관련 통합 테스트")
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CookieUtil cookieUtil;

    @MockitoBean
    private SocialUnlinkService socialUnlinkService;

    @MockitoBean
    private SocialLoginRepository socialLoginRepository;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private User testUser;
    private String validToken;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        validToken = "valid-jwt-token";

        // 테스트용 사용자 설정
        testUser = User.builder()
                .email(testEmail)
                .nickname("테스트닉네임")
                .name("홍길동")
                .password("encoded-password")
                .phone("010-1234-5678")
                .build();
        setUserId(testUser, 1L);
        setUserStatus(testUser, UserStatus.ACTIVE);
        setUserType(testUser, UserType.USER);
        setTemperature(testUser, new BigDecimal("36.5"));
    }

    // === 민감한 프로필 업데이트 API 테스트 ===

    @Test
    @DisplayName("민감한 프로필 업데이트 - 성공")
    void updateSensitiveProfile_success() throws Exception {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "currentPassword123!", "new@example.com", "010-1111-2222");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            "new@example.com", "010-1111-2222")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("민감한 정보가 성공적으로 업데이트되었습니다."))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.email").value(testEmail));

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).getUsernameFromToken(validToken);
        verify(userService).updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            "new@example.com", "010-1111-2222");
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 토큰 없음 - 401 Unauthorized")
    void updateSensitiveProfile_noToken_unauthorized() throws Exception {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "currentPassword123!", "new@example.com", null);

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));

        verify(userService, never()).updateSensitiveProfileForEmail(any(), any(), any(), any());
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 유효하지 않은 토큰 - 401 Unauthorized")
    void updateSensitiveProfile_invalidToken_unauthorized() throws Exception {
        // Given
        String invalidToken = "invalid-token";
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "currentPassword123!", "new@example.com", null);

        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", invalidToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));

        verify(jwtUtil).validateToken(invalidToken);
        verify(userService, never()).updateSensitiveProfileForEmail(any(), any(), any(), any());
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 업데이트할 정보 없음 - 400 Bad Request")
    void updateSensitiveProfile_noUpdates_badRequest() throws Exception {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "currentPassword123!", null, null);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("업데이트할 정보가 없습니다."));

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).getUsernameFromToken(validToken);
        verify(userService, never()).updateSensitiveProfileForEmail(any(), any(), any(), any());
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 잘못된 현재 비밀번호 - 401 Unauthorized")
    void updateSensitiveProfile_wrongCurrentPassword_unauthorized() throws Exception {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "wrongPassword", "new@example.com", null);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.updateSensitiveProfileForEmail(testEmail, "wrongPassword", 
            "new@example.com", null)).thenThrow(new CustomException(ErrorCode.LOGIN_FAILED));

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("LOGIN_FAILED"));

        verify(userService).updateSensitiveProfileForEmail(testEmail, "wrongPassword", 
            "new@example.com", null);
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 중복된 이메일 - 400 Bad Request")
    void updateSensitiveProfile_duplicateEmail_badRequest() throws Exception {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "currentPassword123!", "duplicate@example.com", null);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            "duplicate@example.com", null)).thenThrow(new CustomException(ErrorCode.DUPLICATE_EMAIL));

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"));

        verify(userService).updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            "duplicate@example.com", null);
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 중복된 전화번호 - 400 Bad Request")
    void updateSensitiveProfile_duplicatePhone_badRequest() throws Exception {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "currentPassword123!", null, "010-9999-9999");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            null, "010-9999-9999")).thenThrow(new CustomException(ErrorCode.DUPLICATE_PHONE));

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_PHONE"));

        verify(userService).updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            null, "010-9999-9999");
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 유효성 검사 실패 - 400 Bad Request")
    void updateSensitiveProfile_validationFailed_badRequest() throws Exception {
        // Given - 현재 비밀번호 없음
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "", "new@example.com", null);

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateSensitiveProfileForEmail(any(), any(), any(), any());
    }

    // === 비밀번호 변경 API 테스트 ===

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    void changePassword_success() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.changePasswordForEmail(testEmail, "currentPassword123!", "NewPassword123!"))
            .thenReturn(testUser);

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호가 변경되었습니다. 보안을 위해 다시 로그인해주세요."))
                .andExpect(jsonPath("$.data.message").value("비밀번호가 성공적으로 변경되었습니다."))
                .andExpect(jsonPath("$.data.sessionInvalidated").value(true));

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).getUsernameFromToken(validToken);
        verify(userService).changePasswordForEmail(testEmail, "currentPassword123!", "NewPassword123!");
        verify(cookieUtil).clearTokenCookies(any());
    }

    @Test
    @DisplayName("비밀번호 변경 - 토큰 없음 - 401 Unauthorized")
    void changePassword_noToken_unauthorized() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));

        verify(userService, never()).changePasswordForEmail(any(), any(), any());
    }

    @Test
    @DisplayName("비밀번호 변경 - 유효하지 않은 토큰 - 401 Unauthorized")
    void changePassword_invalidToken_unauthorized() throws Exception {
        // Given
        String invalidToken = "invalid-token";
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", invalidToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));

        verify(jwtUtil).validateToken(invalidToken);
        verify(userService, never()).changePasswordForEmail(any(), any(), any());
    }

    @Test
    @DisplayName("비밀번호 변경 - 새 비밀번호와 확인 비밀번호 불일치 - 400 Bad Request")
    void changePassword_passwordMismatch_badRequest() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "DifferentPassword123!");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("새 비밀번호와 확인 비밀번호가 일치하지 않습니다."));

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).getUsernameFromToken(validToken);
        verify(userService, never()).changePasswordForEmail(any(), any(), any());
    }

    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호와 새 비밀번호 동일 - 400 Bad Request")
    void changePassword_sameAsCurrentPassword_badRequest() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "SamePassword123!", "SamePassword123!", "SamePassword123!");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_RESET_SAME_AS_OLD"))
                .andExpect(jsonPath("$.message").value("새 비밀번호는 현재 비밀번호와 달라야 합니다."));

        verify(jwtUtil).validateToken(validToken);
        verify(jwtUtil).getUsernameFromToken(validToken);
        verify(userService, never()).changePasswordForEmail(any(), any(), any());
    }

    @Test
    @DisplayName("비밀번호 변경 - 잘못된 현재 비밀번호 - 401 Unauthorized")
    void changePassword_wrongCurrentPassword_unauthorized() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "wrongPassword", "NewPassword123!", "NewPassword123!");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.changePasswordForEmail(testEmail, "wrongPassword", "NewPassword123!"))
            .thenThrow(new CustomException(ErrorCode.LOGIN_FAILED));

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("LOGIN_FAILED"));

        verify(userService).changePasswordForEmail(testEmail, "wrongPassword", "NewPassword123!");
    }

    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호와 동일한 새 비밀번호 (서비스 레벨) - 400 Bad Request")
    void changePassword_sameAsCurrentPasswordServiceLevel_badRequest() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.changePasswordForEmail(testEmail, "currentPassword123!", "NewPassword123!"))
            .thenThrow(new CustomException(ErrorCode.PASSWORD_RESET_SAME_AS_OLD));

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("PASSWORD_RESET_SAME_AS_OLD"));

        verify(userService).changePasswordForEmail(testEmail, "currentPassword123!", "NewPassword123!");
    }

    @Test
    @DisplayName("비밀번호 변경 - 유효성 검사 실패 - 400 Bad Request")
    void changePassword_validationFailed_badRequest() throws Exception {
        // Given - 현재 비밀번호 없음
        PasswordChangeRequest request = new PasswordChangeRequest(
            "", "NewPassword123!", "NewPassword123!");

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePasswordForEmail(any(), any(), any());
    }

    @Test
    @DisplayName("비밀번호 변경 - 약한 새 비밀번호 - 400 Bad Request")
    void changePassword_weakNewPassword_badRequest() throws Exception {
        // Given - 약한 비밀번호 (숫자 없음)
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "WeakPassword!", "WeakPassword!");

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePasswordForEmail(any(), any(), any());
    }

    // === 서버 오류 처리 테스트 ===

    @Test
    @DisplayName("민감한 프로필 업데이트 - 서버 내부 오류 - 500 Internal Server Error")
    void updateSensitiveProfile_internalServerError() throws Exception {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "currentPassword123!", "new@example.com", null);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            "new@example.com", null)).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("비밀번호 변경 - 서버 내부 오류 - 500 Internal Server Error")
    void changePassword_internalServerError() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.changePasswordForEmail(testEmail, "currentPassword123!", "NewPassword123!"))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }

    // === 경계값 및 특수 케이스 테스트 ===

    @Test
    @DisplayName("민감한 프로필 업데이트 - 이메일만 업데이트 - 성공")
    void updateSensitiveProfile_emailOnly_success() throws Exception {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "currentPassword123!", "new@example.com", null);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            "new@example.com", null)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            "new@example.com", null);
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 전화번호만 업데이트 - 성공")
    void updateSensitiveProfile_phoneOnly_success() throws Exception {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "currentPassword123!", null, "010-1111-2222");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            null, "010-1111-2222")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).updateSensitiveProfileForEmail(testEmail, "currentPassword123!", 
            null, "010-1111-2222");
    }

    @Test
    @DisplayName("비밀번호 변경 - 복잡한 새 비밀번호 - 성공")
    void changePassword_complexNewPassword_success() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "MyC0mpl3x@P4ssw0rd!", "MyC0mpl3x@P4ssw0rd!");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenReturn(testEmail);
        when(userService.changePasswordForEmail(testEmail, "currentPassword123!", "MyC0mpl3x@P4ssw0rd!"))
            .thenReturn(testUser);

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).changePasswordForEmail(testEmail, "currentPassword123!", "MyC0mpl3x@P4ssw0rd!");
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - JWT 토큰 예외 처리 - 401 Unauthorized")
    void updateSensitiveProfile_jwtException_unauthorized() throws Exception {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "currentPassword123!", "new@example.com", null);

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenThrow(new RuntimeException("JWT parsing error"));

        // When & Then
        mockMvc.perform(put("/api/users/profile/sensitive")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));

        verify(userService, never()).updateSensitiveProfileForEmail(any(), any(), any(), any());
    }

    @Test
    @DisplayName("비밀번호 변경 - JWT 토큰 예외 처리 - 401 Unauthorized")
    void changePassword_jwtException_unauthorized() throws Exception {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        when(jwtUtil.validateToken(validToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validToken)).thenThrow(new RuntimeException("JWT parsing error"));

        // When & Then
        mockMvc.perform(put("/api/users/password")
                .cookie(new Cookie("accessToken", validToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));

        verify(userService, never()).changePasswordForEmail(any(), any(), any());
    }

    // === 헬퍼 메서드 ===

    /**
     * 리플렉션을 사용하여 User 엔티티의 userId를 설정하는 헬퍼 메서드
     */
    private void setUserId(User user, Long userId) {
        try {
            java.lang.reflect.Field userIdField = User.class.getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.set(user, userId);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
    }

    /**
     * 리플렉션을 사용하여 User 엔티티의 userStatus를 설정하는 헬퍼 메서드
     */
    private void setUserStatus(User user, UserStatus userStatus) {
        try {
            java.lang.reflect.Field userStatusField = User.class.getDeclaredField("userStatus");
            userStatusField.setAccessible(true);
            userStatusField.set(user, userStatus);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
    }

    /**
     * 리플렉션을 사용하여 User 엔티티의 userType을 설정하는 헬퍼 메서드
     */
    private void setUserType(User user, UserType userType) {
        try {
            java.lang.reflect.Field userTypeField = User.class.getDeclaredField("userType");
            userTypeField.setAccessible(true);
            userTypeField.set(user, userType);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
    }

    /**
     * 리플렉션을 사용하여 User 엔티티의 temperature를 설정하는 헬퍼 메서드
     */
    private void setTemperature(User user, BigDecimal temperature) {
        try {
            java.lang.reflect.Field temperatureField = User.class.getDeclaredField("temperature");
            temperatureField.setAccessible(true);
            temperatureField.set(user, temperature);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
    }
}