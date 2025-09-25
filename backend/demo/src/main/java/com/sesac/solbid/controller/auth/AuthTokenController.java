package com.sesac.solbid.controller.auth;

import com.sesac.solbid.dto.api.ApiResponse;
import com.sesac.solbid.service.user.UserService;
import com.sesac.solbid.util.JwtUtil;
import com.sesac.solbid.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

/**
 * JWT 토큰 관리 컨트롤러
 * <p>
 * JWT 토큰의 갱신, 상태 확인 등 토큰 관련 기능을 제공하는 컨트롤러입니다.
 * 리프레시 토큰을 통한 액세스 토큰 재발급과 토큰 상태 조회를 담당합니다.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthTokenController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final CookieUtil cookieUtil;

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     * <p>
     * 만료된 액세스 토큰을 리프레시 토큰을 사용하여 재발급합니다.
     * 토큰 회전(Token Rotation) 방식을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 모두 발급합니다.
     * </p>
     *
     * @param request HTTP 요청 (쿠키에 refreshToken 포함)
     * @param response HTTP 응답 (새 토큰 쿠키 설정)
     * @return 액세스 토큰 재발급 결과 및 토큰 만료 시간 정보
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String refreshToken = extractRefreshTokenCookie(request);
            if (refreshToken == null || refreshToken.isBlank()) {
                log.warn("리프레시 토큰 없음");
                return ResponseEntity.status(401).body(
                        ApiResponse.error("UNAUTHORIZED", "리프레시 토큰이 없습니다.")
                );
            }

            // 리프레시 토큰 유효성 검사 (서명, 만료)
            if (!jwtUtil.validateToken(refreshToken)) {
                log.warn("리프레시 토큰 유효성 실패");
                return ResponseEntity.status(401).body(
                        ApiResponse.error("UNAUTHORIZED", "유효하지 않거나 만료된 리프레시 토큰입니다.")
                );
            }

            // 주체 추출 (이메일)
            String email = jwtUtil.getUsernameFromToken(refreshToken);
            if (email == null || email.isBlank()) {
                log.warn("리프레시 토큰에서 이메일 추출 실패");
                return ResponseEntity.status(401).body(
                        ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다.")
                );
            }

            // 사용자 존재 확인 (상태 확인은 서비스 정책에 따라 확장 가능)
            try {
                userService.getByEmail(email);
            } catch (Exception e) {
                log.warn("리프레시 토큰의 사용자 미존재: {}", email);
                return ResponseEntity.status(401).body(
                        ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다.")
                );
            }

            // 새 액세스/리프레시 토큰 발급 (회전)
            String newAccessToken = jwtUtil.generateToken(email);
            String newRefreshToken = jwtUtil.generateRefreshToken(email);

            // 쿠키 재설정 (CookieUtil 사용)
            cookieUtil.addTokenCookies(
                    response,
                    newAccessToken, jwtUtil.getAccessTokenValiditySeconds(),
                    newRefreshToken, jwtUtil.getRefreshTokenValiditySeconds()
            );

            Map<String, Object> body = new HashMap<>();
            body.put("accessTokenExpiresIn", jwtUtil.getAccessTokenValiditySeconds());
            body.put("refreshTokenExpiresIn", jwtUtil.getRefreshTokenValiditySeconds());

            return ResponseEntity.ok(ApiResponse.success(body, "토큰이 갱신되었습니다."));
        } catch (Exception e) {
            log.error("토큰 갱신 처리 중 예외", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 액세스 토큰 상태 조회
     * <p>
     * 현재 액세스 토큰의 유효성과 만료까지 남은 시간을 확인합니다.
     * 프론트엔드에서 토큰 만료 전 선제적 갱신을 위해 사용됩니다.
     * </p>
     * 
     * @param request HTTP 요청 (JWT 토큰 쿠키 포함)
     * @return 토큰 상태 정보 (인증 여부, 만료 시간, 리프레시 토큰 보유 여부)
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> tokenStatus(HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        try {
            String accessToken = extractCookie(request, "accessToken");
            boolean hasRefresh = extractCookie(request, "refreshToken") != null;
            if (accessToken == null || accessToken.isBlank()) {
                body.put("isAuthenticated", false);
                body.put("accessTokenExpiresIn", 0);
                body.put("refreshAvailable", hasRefresh);
                return ResponseEntity.ok(ApiResponse.success(body));
            }

            boolean valid = jwtUtil.validateToken(accessToken);
            if (!valid) {
                body.put("isAuthenticated", false);
                body.put("accessTokenExpiresIn", 0);
                body.put("refreshAvailable", hasRefresh);
                return ResponseEntity.ok(ApiResponse.success(body));
            }

            Date exp = jwtUtil.getExpirationDateFromToken(accessToken);
            long remainingSeconds = Math.max(0L, (exp.getTime() - System.currentTimeMillis()) / 1000L);

            body.put("isAuthenticated", true);
            body.put("accessTokenExpiresIn", remainingSeconds);
            body.put("refreshAvailable", hasRefresh);
            return ResponseEntity.ok(ApiResponse.success(body));
        } catch (Exception e) {
            // 폴링 엔드포인트 특성상 200 + isAuthenticated=false로 응답하여 프론트에서 부드럽게 처리
            log.debug("/api/auth/status 처리 중 예외", e);
            body.put("isAuthenticated", false);
            body.put("accessTokenExpiresIn", 0);
            body.put("refreshAvailable", false);
            return ResponseEntity.ok(ApiResponse.success(body));
        }
    }

    /**
     * HTTP 요청에서 리프레시 토큰 쿠키를 추출합니다.
     * 
     * @param request HTTP 요청
     * @return 리프레시 토큰 값 (없으면 null)
     */
    private String extractRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("refreshToken".equals(c.getName())) return c.getValue();
        }
        return null;
    }

    /**
     * HTTP 요청에서 특정 이름의 쿠키 값을 추출합니다.
     * 
     * @param request HTTP 요청
     * @param name 쿠키 이름
     * @return 쿠키 값 (없으면 null)
     */
    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
