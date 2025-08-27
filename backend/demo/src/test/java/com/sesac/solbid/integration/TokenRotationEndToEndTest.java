package com.sesac.solbid.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.repository.UserRepository;
import com.sesac.solbid.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("리프레시 토큰 기반 액세스 토큰 회전 E2E 테스트")
class TokenRotationEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("만료된 accessToken + 유효한 refreshToken -> /refresh 후 /me 성공")
    void tokenRotationFlow_Success() throws Exception {
        // Given: 유저 1명 생성
        User user = User.builder()
                .email("rotation@test.com")
                .nickname("로테이션유저")
                .build();
        userRepository.save(user);

        // 만료된 accessToken (과거 만료)
        String expiredAccess = TestJwtFactory.generateExpiredAccessToken(jwtUtil, user.getEmail());
        // 유효한 refreshToken (JwtUtil로 발급)
        String validRefresh = jwtUtil.generateRefreshToken(user.getEmail());

        // 1) 만료된 accessToken으로 /me 호출 -> 401
        mockMvc.perform(get("/api/users/me")
                        .cookie(new Cookie("accessToken", expiredAccess)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        // 2) refresh 쿠키로 /api/auth/refresh 호출 -> 새 토큰 쿠키 수신
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .cookie(new Cookie("refreshToken", validRefresh)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        String newAccess = getCookieValue(refreshResult, "accessToken");
        String newRefresh = getCookieValue(refreshResult, "refreshToken");
        assertThat(newAccess).isNotBlank();
        assertThat(newRefresh).isNotBlank();

        // 3) 새 accessToken으로 /me 호출 -> 200 OK
        mockMvc.perform(get("/api/users/me")
                        .cookie(new Cookie("accessToken", newAccess)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("rotation@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("로테이션유저"));
    }

    private String getCookieValue(MvcResult result, String name) {
        jakarta.servlet.http.Cookie[] cookies = result.getResponse().getCookies();
        if (cookies == null) return null;
        for (jakarta.servlet.http.Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
