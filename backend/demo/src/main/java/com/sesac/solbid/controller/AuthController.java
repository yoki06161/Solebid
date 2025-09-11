package com.sesac.solbid.controller;

import com.sesac.solbid.dto.ApiResponse;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.exception.OAuth2Exception;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ReactivationRequiredException;
import com.sesac.solbid.service.auth.OAuth2Service;
import com.sesac.solbid.service.user.PasswordResetService;
import com.sesac.solbid.util.JwtUtil;
import com.sesac.solbid.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sesac.solbid.dto.auth.response.AuthUrlResponse;
import com.sesac.solbid.dto.auth.request.CallbackRequest;
import com.sesac.solbid.dto.auth.response.LoginSuccessResponse;
import com.sesac.solbid.dto.user.response.LoginResponse;
import com.sesac.solbid.dto.auth.request.PasswordResetRequest;
import com.sesac.solbid.dto.auth.request.PasswordResetConfirmRequest;

/**
 * 인증 컨트롤러
 * 소셜로그인 URL 생성 및 콜백 처리를 담당
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final OAuth2Service oAuth2Service;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final PasswordResetService passwordResetService;

    /**
     * 로그아웃 처리
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(HttpServletResponse response) {

        log.info("로그아웃 요청");
        
        try {
            // 쿠키 삭제
            cookieUtil.clearTokenCookies(response);

            log.info("로그아웃 완료");
            
            return ResponseEntity.ok(
                ApiResponse.success(Collections.emptyMap(), "로그아웃이 완료되었습니다.")
            );
            
        } catch (Exception e) {
            log.error("로그아웃 처리 중 예외 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.")
            );
        }
    }

    /**
     * OAuth2 인증 URL 생성
     * GET /api/auth/oauth2/{provider}/url
     * 
     * @param provider 소셜 플랫폼 이름 (google, kakao)
     * @return 인증 URL과 state 정보
     */
    @GetMapping("/oauth2/{provider}/url")
    public ResponseEntity<ApiResponse<AuthUrlResponse>> generateAuthUrl(
            @PathVariable String provider,
            HttpServletRequest request) {
        
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        log.info("OAuth2 인증 URL 생성 요청: provider={}, clientIp={}, userAgent={}", 
                provider, clientIp, maskUserAgent(userAgent));
        
        try {
            AuthUrlResponse response = oAuth2Service.generateAuthUrl(provider);

            log.info("OAuth2 인증 URL 생성 성공: provider={}, clientIp={}, state={}", 
                    provider, clientIp, maskState(response.getState()));
            
            return ResponseEntity.ok(
                ApiResponse.success(response, "OAuth2 인증 URL이 생성되었습니다.")
            );
            
        } catch (OAuth2Exception e) {
            log.warn("OAuth2 인증 URL 생성 실패: provider={}, clientIp={}, error={}", 
                    provider, clientIp, e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getErrorCode().name(), e.getMessage())
            );
        } catch (Exception e) {
            log.error("OAuth2 인증 URL 생성 중 예외 발생: provider={}, clientIp={}", 
                    provider, clientIp, e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.")
            );
        }
    }

    /**
     * OAuth2 콜백 처리
     * POST /api/auth/oauth2/{provider}/callback
     * 
     * @param provider 소셜 플랫폼 이름 (google, kakao)
     * @param request 콜백 요청 (code, state)
     * @return 로그인 응답 (JWT 토큰 포함)
     */
    @PostMapping("/oauth2/{provider}/callback")
    public ResponseEntity<ApiResponse<Object>> handleCallback(
            @PathVariable String provider,
            @Valid @RequestBody CallbackRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        String clientIp = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        log.info("OAuth2 콜백 처리 요청: provider={}, clientIp={}, userAgent={}, state={}", 
                provider, clientIp, maskUserAgent(userAgent), maskState(request.getState()));
        
        try {
            LoginResponse response = oAuth2Service.processCallback(
                provider, request.getCode(), request.getState()
            );
            
            // HttpOnly 쿠키로 토큰 설정
            cookieUtil.addTokenCookies(
                httpResponse,
                response.getAccessToken(), jwtUtil.getAccessTokenValiditySeconds(),
                response.getRefreshToken(), jwtUtil.getRefreshTokenValiditySeconds()
            );

            // 임시 닉네임인지 여부 판단 (user_ 접두어)
            boolean requiresNickname = response.getNickname() != null && response.getNickname().startsWith("user_");

            // 응답에서는 토큰 제외하고 사용자 정보만 반환
            LoginSuccessResponse loginSuccessResponse = LoginSuccessResponse.builder()
                    .userId(response.getUserId())
                    .email(response.getEmail())
                    .nickname(response.getNickname())
                    .userType(response.getUserType())
                    .provider(provider)
                    .requiresNickname(requiresNickname)
                    .build();
            
            log.info("OAuth2 콜백 처리 성공: provider={}, clientIp={}, userId={}, email={}", 
                    provider, clientIp, response.getUserId(), maskEmail(response.getEmail()));
            
            return ResponseEntity.ok(
                ApiResponse.success(loginSuccessResponse, "소셜로그인이 완료되었습니다.")
            );
        } catch (OAuth2Exception e) {
            log.warn("OAuth2 콜백 처리 실패: provider={}, clientIp={}, error={}, state={}", 
                    provider, clientIp, e.getMessage(), maskState(request.getState()));
            int status = (e.getErrorCode() == ErrorCode.SOCIAL_ACCOUNT_CONFLICT) ? 409 : 400;
            return ResponseEntity.status(status).body(
                ApiResponse.error(e.getErrorCode().name(), e.getMessage())
            );
        } catch (ReactivationRequiredException e) {
            log.warn("OAuth2 콜백 - 탈퇴 계정: provider={}, email=***", provider);
            String token = jwtUtil.generateReactivationToken(e.getEmail(), 600); // 10분
            Map<String, Object> data = new HashMap<>();
            data.put("reactivationToken", token);
            data.put("email", e.getEmail());
            return ResponseEntity.status(401).body(
                ApiResponse.error(data, "WITHDRAWN_USER", "회원 탈퇴 처리된 계정입니다. 계정을 다시 활성화하시겠습니까?")
            );
        } catch (CustomException e) {
            log.warn("OAuth2 콜백 처리 실패(Custom): provider={}, clientIp={}, error={}, state={}",
                    provider, clientIp, e.getMessage(), maskState(request.getState()));
            return ResponseEntity.status(e.getErrorCode().getStatus()).body(
                    ApiResponse.error(e.getErrorCode().name(), e.getMessage())
            );
        } catch (Exception e) {
            log.error("OAuth2 콜백 처리 중 예외 발생: provider={}, clientIp={}, state={}", 
                    provider, clientIp, maskState(request.getState()), e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 비밀번호 재설정 요청
     * POST /api/auth/password/forgot
     */
    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<Object>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        try {
            passwordResetService.requestReset(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyMap(), "비밀번호 재설정 메일을 발송했습니다."));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 비밀번호 재설정 확인 및 변경
     * POST /api/auth/password/reset
     */
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyMap(), "비밀번호가 재설정되었습니다."));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 클라이언트 IP 주소 추출 (프록시 고려)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * User-Agent 마스킹 처리 (보안)
     */
    private String maskUserAgent(String userAgent) {
        if (userAgent == null || userAgent.length() < 20) {
            return "****";
        }
        return userAgent.substring(0, 10) + "****" + userAgent.substring(userAgent.length() - 6);
    }

    /**
     * State 값 마스킹 처리 (보안)
     */
    private String maskState(String state) {
        if (state == null || state.length() < 8) {
            return "****";
        }
        return state.substring(0, 4) + "****" + state.substring(state.length() - 4);
    }


    /**
     * 이메일 마스킹 처리 (보안)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "**@" + domain;
        }

        return localPart.substring(0, 2) + "****@" + domain;
    }
}