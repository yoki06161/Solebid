package com.sesac.solbid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.controller.user.UserController;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.UserStatus;
import com.sesac.solbid.domain.enums.UserType;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.exception.GlobalExceptionHandler;
import com.sesac.solbid.repository.auth.SocialLoginRepository;
import com.sesac.solbid.security.CustomUserDetailsService;
import com.sesac.solbid.service.auth.SocialUnlinkService;
import com.sesac.solbid.service.user.UserService;
import com.sesac.solbid.util.CookieUtil;
import com.sesac.solbid.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 프로필 업데이트 API 통합 테스트
 * <p>
 * 프로필 업데이트 엔드포인트의 전체적인 동작을 테스트합니다.
 * 인증, 유효성 검사, 비즈니스 로직, 응답 형식 등을 확인합니다.
 * </p>
 */
@WebMvcTest(controllers = UserController.class)
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CookieUtil.class})
@DisplayName("UserController 프로필 업데이트 API 통합 테스트")
class UserControllerProfileUpdateTest {

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

    private User testUser;
    private String validAccessToken;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        validAccessToken = "valid.jwt.token";

        // 테스트용 사용자 생성
        testUser = User.builder()
                .email(testEmail)
                .nickname("기존닉네임")
                .name("홍길동")
                .password("encoded-password")
                .phone("010-1234-5678")
                .build();
        setUserId(testUser, 1L);
        setUserStatus(testUser, UserStatus.ACTIVE);
        setUserType(testUser, UserType.USER);
        setTemperature(testUser, new BigDecimal("36.5"));
    }

    // === 성공적인 프로필 업데이트 테스트 ===

    @Test
    @DisplayName("모든 필드 업데이트 - 성공")
    void updateProfile_allFields_success() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": "새닉네임",
                    "name": "새이름"
                }
                """;

        User updatedUser = User.builder()
                .email(testEmail)
                .nickname("새닉네임")
                .name("새이름")
                .password("encoded-password")
                .phone("010-1234-5678")
                .build();
        setUserId(updatedUser, 1L);
        setUserStatus(updatedUser, UserStatus.ACTIVE);
        setUserType(updatedUser, UserType.USER);
        setTemperature(updatedUser, new BigDecimal("36.5"));

        when(jwtUtil.validateToken(validAccessToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validAccessToken)).thenReturn(testEmail);
        when(userService.updateProfileForEmail(testEmail, "새닉네임", "새이름"))
                .thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.email").value(testEmail))
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.data.name").value("새이름"))
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.userType").value("USER"))
                .andExpect(jsonPath("$.data.temperature").value(36.5))
                .andExpect(jsonPath("$.message").value("프로필이 성공적으로 업데이트되었습니다."));

        verify(jwtUtil).validateToken(validAccessToken);
        verify(jwtUtil).getUsernameFromToken(validAccessToken);
        verify(userService).updateProfileForEmail(testEmail, "새닉네임", "새이름");
    }

    @Test
    @DisplayName("닉네임만 업데이트 - 성공")
    void updateProfile_nicknameOnly_success() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": "새닉네임"
                }
                """;

        User updatedUser = User.builder()
                .email(testEmail)
                .nickname("새닉네임")
                .name("홍길동")
                .password("encoded-password")
                .phone("010-1234-5678")
                .build();
        setUserId(updatedUser, 1L);
        setUserStatus(updatedUser, UserStatus.ACTIVE);
        setUserType(updatedUser, UserType.USER);
        setTemperature(updatedUser, new BigDecimal("36.5"));

        when(jwtUtil.validateToken(validAccessToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validAccessToken)).thenReturn(testEmail);
        when(userService.updateProfileForEmail(testEmail, "새닉네임", null))
                .thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.data.name").value("홍길동")) // 기존 값 유지
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678")) // 기존 값 유지
                .andExpect(jsonPath("$.message").value("프로필이 성공적으로 업데이트되었습니다."));

        verify(userService).updateProfileForEmail(testEmail, "새닉네임", null);
    }

    @Test
    @DisplayName("이름만 업데이트 - 성공")
    void updateProfile_nameOnly_success() throws Exception {
        // Given
        String requestBody = """
                {
                    "name": "새이름"
                }
                """;

        User updatedUser = User.builder()
                .email(testEmail)
                .nickname("기존닉네임")
                .name("새이름")
                .password("encoded-password")
                .phone("010-1234-5678")
                .build();
        setUserId(updatedUser, 1L);
        setUserStatus(updatedUser, UserStatus.ACTIVE);
        setUserType(updatedUser, UserType.USER);
        setTemperature(updatedUser, new BigDecimal("36.5"));

        when(jwtUtil.validateToken(validAccessToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validAccessToken)).thenReturn(testEmail);
        when(userService.updateProfileForEmail(testEmail, null, "새이름"))
                .thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("기존닉네임")) // 기존 값 유지
                .andExpect(jsonPath("$.data.name").value("새이름"))
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678")) // 기존 값 유지
                .andExpect(jsonPath("$.message").value("프로필이 성공적으로 업데이트되었습니다."));

        verify(userService).updateProfileForEmail(testEmail, null, "새이름");
    }

    @Test
    @DisplayName("모든 필드 null - 성공 (변경 없음)")
    void updateProfile_allFieldsNull_success() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": null,
                    "name": null
                }
                """;

        when(jwtUtil.validateToken(validAccessToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validAccessToken)).thenReturn(testEmail);
        when(userService.updateProfileForEmail(testEmail, null, null))
                .thenReturn(testUser);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("기존닉네임"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.message").value("프로필이 성공적으로 업데이트되었습니다."));

        verify(userService).updateProfileForEmail(testEmail, null, null);
    }

    // === 인증 관련 테스트 ===

    @Test
    @DisplayName("액세스 토큰 없음 - 401 Unauthorized")
    void updateProfile_noAccessToken_unauthorized() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": "새닉네임"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));

        verify(userService, never()).updateProfileForEmail(any(), any(), any());
    }

    @Test
    @DisplayName("유효하지 않은 토큰 - 401 Unauthorized")
    void updateProfile_invalidToken_unauthorized() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": "새닉네임"
                }
                """;
        String invalidToken = "invalid.jwt.token";

        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("accessToken", invalidToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));

        verify(jwtUtil).validateToken(invalidToken);
        verify(userService, never()).updateProfileForEmail(any(), any(), any());
    }

    // === 유효성 검사 테스트 ===

    @Test
    @DisplayName("닉네임 너무 짧음 - 400 Bad Request")
    void updateProfile_nicknameTooShort_badRequest() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": "닉"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("닉네임은 2-50자 사이여야 합니다")));

        verify(userService, never()).updateProfileForEmail(any(), any(), any());
    }

    @Test
    @DisplayName("이름 너무 짧음 - 400 Bad Request")
    void updateProfile_nameTooShort_badRequest() throws Exception {
        // Given
        String requestBody = """
                {
                    "name": "홍"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message", containsString("이름은 2-50자 사이여야 합니다")));

        verify(userService, never()).updateProfileForEmail(any(), any(), any());
    }

    // === 비즈니스 로직 예외 테스트 ===

    @Test
    @DisplayName("중복된 닉네임 - 400 Bad Request")
    void updateProfile_duplicateNickname_badRequest() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": "중복닉네임"
                }
                """;

        when(jwtUtil.validateToken(validAccessToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validAccessToken)).thenReturn(testEmail);
        when(userService.updateProfileForEmail(testEmail, "중복닉네임", null))
                .thenThrow(new CustomException(ErrorCode.DUPLICATE_NICKNAME));

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_NICKNAME"));

        verify(userService).updateProfileForEmail(testEmail, "중복닉네임", null);
    }

    @Test
    @DisplayName("사용자 찾을 수 없음 - 401 Unauthorized")
    void updateProfile_userNotFound_unauthorized() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": "새닉네임"
                }
                """;

        when(jwtUtil.validateToken(validAccessToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validAccessToken)).thenReturn(testEmail);
        when(userService.updateProfileForEmail(testEmail, "새닉네임", null))
                .thenThrow(new CustomException(ErrorCode.LOGIN_FAILED));

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("LOGIN_FAILED"));

        verify(userService).updateProfileForEmail(testEmail, "새닉네임", null);
    }

    // === 서버 오류 테스트 ===

    @Test
    @DisplayName("서버 내부 오류 - 500 Internal Server Error")
    void updateProfile_internalServerError_serverError() throws Exception {
        // Given
        String requestBody = """
                {
                    "nickname": "새닉네임"
                }
                """;

        when(jwtUtil.validateToken(validAccessToken)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(validAccessToken)).thenReturn(testEmail);
        when(userService.updateProfileForEmail(testEmail, "새닉네임", null))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));

        verify(userService).updateProfileForEmail(testEmail, "새닉네임", null);
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