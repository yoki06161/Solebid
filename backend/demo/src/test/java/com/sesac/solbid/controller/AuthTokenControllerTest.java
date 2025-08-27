package com.sesac.solbid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.config.WebConfig;
import com.sesac.solbid.exception.GlobalExceptionHandler;
import com.sesac.solbid.security.SecurityConfig;
import com.sesac.solbid.service.UserService;
import com.sesac.solbid.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthTokenController.class)
@TestPropertySource(properties = "spring.main.web-application-type=servlet")
@Import({WebConfig.class, GlobalExceptionHandler.class, SecurityConfig.class})
@DisplayName("AuthTokenController 리프레시 토큰 갱신 테스트")
class AuthTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("리프레시 토큰 갱신 성공")
    void refreshToken_Success() throws Exception {
        String refresh = "valid-refresh";
        String email = "test@example.com";
        when(jwtUtil.validateToken(refresh)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(refresh)).thenReturn(email);
        // 사용자 존재 확인만 통과하면 되므로 반환값은 사용되지 않음
        when(userService.getByEmail(email)).thenReturn(mock(com.sesac.solbid.domain.User.class));
        when(jwtUtil.generateToken(email)).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken(email)).thenReturn("new-refresh");
        when(jwtUtil.getAccessTokenValiditySeconds()).thenReturn(3600L);
        when(jwtUtil.getRefreshTokenValiditySeconds()).thenReturn(86400L);

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .cookie(new Cookie("refreshToken", refresh)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("토큰이 갱신되었습니다."))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().value("accessToken", "new-access"))
                .andExpect(cookie().value("refreshToken", "new-refresh"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().path("accessToken", "/"))
                .andExpect(cookie().path("refreshToken", "/"))
                .andExpect(cookie().maxAge("accessToken", 3600))
                .andExpect(cookie().maxAge("refreshToken", 86400))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(3600))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn").value(86400));

        verify(jwtUtil).validateToken(refresh);
        verify(jwtUtil).getUsernameFromToken(refresh);
        verify(userService).getByEmail(email);
        verify(jwtUtil).generateToken(email);
        verify(jwtUtil).generateRefreshToken(email);
    }

    @Test
    @DisplayName("리프레시 토큰 없음 -> 401")
    void refreshToken_MissingCookie() throws Exception {
        mockMvc.perform(post("/api/auth/refresh").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 없습니다."));

        verify(jwtUtil, never()).validateToken(any());
    }

    @Test
    @DisplayName("리프레시 토큰 유효성 실패 -> 401")
    void refreshToken_InvalidRefresh() throws Exception {
        when(jwtUtil.validateToken("bad")).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .cookie(new Cookie("refreshToken", "bad")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("유효하지 않거나 만료된 리프레시 토큰입니다."));
    }

    @Test
    @DisplayName("사용자 미존재 -> 401")
    void refreshToken_UserNotFound() throws Exception {
        String refresh = "valid-refresh";
        String email = "no@ex.com";
        when(jwtUtil.validateToken(refresh)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(refresh)).thenReturn(email);
        when(userService.getByEmail(email)).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .cookie(new Cookie("refreshToken", refresh)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
    }
}

