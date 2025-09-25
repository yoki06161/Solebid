package com.sesac.solbid.controller.auth;

import com.sesac.solbid.dto.api.ApiResponse;
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
import com.sesac.solbid.dto.auth.request.PasswordResetOtpVerifyRequest;
import com.sesac.solbid.dto.auth.request.PasswordResetVerifyRequest;
import com.sesac.solbid.dto.auth.request.ResendOtpRequest;


/**
 * 인증 컨트롤러
 * <p>
 * OAuth2 소셜 로그인, 비밀번호 재설정, 로그아웃 등 인증 관련 기능을 제공하는 컨트롤러입니다.
 * Google, Kakao 등의 소셜 로그인 URL 생성 및 콜백 처리를 담당합니다.
 * </p>
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
     * <p>
     * 사용자의 로그아웃 요청을 처리하고 JWT 토큰 쿠키를 삭제합니다.
     * </p>
     * 
     * @param response HTTP 응답 (쿠키 삭제를 위해 사용)
     * @return 로그아웃 처리 결과
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
     * <p>
     * 지정된 소셜 로그인 제공자의 OAuth2 인증 URL을 생성합니다.
     * 클라이언트는 이 URL로 사용자를 리다이렉트하여 소셜 로그인을 진행할 수 있습니다.
     * </p>
     * 
     * @param provider 소셜 플랫폼 이름 (google, kakao)
     * @param request HTTP 요청 (클라이언트 IP 및 User-Agent 로깅용)
     * @return OAuth2 인증 URL과 state 정보를 포함한 응답
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
                    provider, clientIp, maskState(response.state()));
            
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
     * <p>
     * OAuth2 제공자로부터 받은 인증 코드를 처리하여 사용자 로그인을 완료합니다.
     * 성공 시 JWT 토큰을 HttpOnly 쿠키로 설정하고 사용자 정보를 반환합니다.
     * </p>
     * 
     * @param provider 소셜 플랫폼 이름 (google, kakao)
     * @param request 콜백 요청 (인증 코드와 state 포함)
     * @param httpRequest HTTP 요청 (클라이언트 IP 및 User-Agent 로깅용)
     * @param httpResponse HTTP 응답 (JWT 토큰 쿠키 설정용)
     * @return 로그인 성공 시 사용자 정보, 실패 시 에러 정보
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
                provider, clientIp, maskUserAgent(userAgent), maskState(request.state()));
        
        try {
            LoginResponse response = oAuth2Service.processCallback(
                provider, request.code(), request.state()
            );
            
            // HttpOnly 쿠키로 토큰 설정
            cookieUtil.addTokenCookies(
                httpResponse,
                response.accessToken(), jwtUtil.getAccessTokenValiditySeconds(),
                response.refreshToken(), jwtUtil.getRefreshTokenValiditySeconds()
            );

            // 임시 닉네임인지 여부 판단 (user_ 접두어)
            boolean requiresNickname = response.nickname() != null && response.nickname().startsWith("user_");

            // 응답에서는 토큰 제외하고 사용자 정보만 반환
            LoginSuccessResponse loginSuccessResponse = new LoginSuccessResponse(
                    response.userId(),
                    response.email(),
                    response.nickname(),
                    response.userType(),
                    provider,
                    requiresNickname
            );
            
            log.info("OAuth2 콜백 처리 성공: provider={}, clientIp={}, userId={}, email={}", 
                    provider, clientIp, response.userId(), maskEmail(response.email()));
            
            return ResponseEntity.ok(
                ApiResponse.success(loginSuccessResponse, "소셜로그인이 완료되었습니다.")
            );
        } catch (OAuth2Exception e) {
            log.warn("OAuth2 콜백 처리 실패: provider={}, clientIp={}, error={}, state={}", 
                    provider, clientIp, e.getMessage(), maskState(request.state()));
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
                    provider, clientIp, e.getMessage(), maskState(request.state()));
            return ResponseEntity.status(e.getErrorCode().getStatus()).body(
                    ApiResponse.error(e.getErrorCode().name(), e.getMessage())
            );
        } catch (Exception e) {
            log.error("OAuth2 콜백 처리 중 예외 발생: provider={}, clientIp={}, state={}", 
                    provider, clientIp, maskState(request.state()), e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 비밀번호 재설정 OTP 요청
     * <p>
     * 사용자의 이메일로 비밀번호 재설정용 OTP 인증번호를 발송합니다.
     * </p>
     * 
     * @param request 비밀번호 재설정 요청 (이메일 포함)
     * @param httpRequest HTTP 요청 (클라이언트 IP 로깅용)
     * @return OTP 발송 결과
     */
    @PostMapping("/password/request-reset")
    public ResponseEntity<ApiResponse<Object>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        log.info("비밀번호 재설정 OTP 요청: email={}, clientIp={}", maskEmail(request.email()), clientIp);
        
        try {
            passwordResetService.requestResetWithOtp(request.email());
            
            log.info("비밀번호 재설정 OTP 발송 완료: email={}, clientIp={}", maskEmail(request.email()), clientIp);
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyMap(), "비밀번호 재설정 인증번호를 이메일로 발송했습니다."));
            
        } catch (CustomException e) {
            log.warn("비밀번호 재설정 OTP 요청 실패: email={}, clientIp={}, error={}", 
                    maskEmail(request.email()), clientIp, e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 재설정 OTP 요청 중 예외 발생: email={}, clientIp={}", 
                    maskEmail(request.email()), clientIp, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 비밀번호 재설정 OTP 검증만 수행
     * <p>
     * 이메일로 발송된 OTP 인증번호의 유효성만 검증합니다.
     * 비밀번호 변경은 수행하지 않습니다.
     * </p>
     * 
     * @param request OTP 검증 요청 (이메일과 OTP 포함)
     * @param httpRequest HTTP 요청 (클라이언트 IP 로깅용)
     * @return OTP 검증 결과
     */
    @PostMapping("/password/verify-otp")
    public ResponseEntity<ApiResponse<Object>> verifyPasswordResetOtp(
            @Valid @RequestBody PasswordResetOtpVerifyRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        log.info("비밀번호 재설정 OTP 검증 요청: email={}, clientIp={}", maskEmail(request.email()), clientIp);
        
        try {
            passwordResetService.verifyOtpOnly(request.email(), request.otp());
            
            log.info("비밀번호 재설정 OTP 검증 성공: email={}, clientIp={}", maskEmail(request.email()), clientIp);
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyMap(), "인증번호가 확인되었습니다."));
            
        } catch (CustomException e) {
            log.warn("비밀번호 재설정 OTP 검증 실패: email={}, clientIp={}, error={}", 
                    maskEmail(request.email()), clientIp, e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 재설정 OTP 검증 중 예외 발생: email={}, clientIp={}", 
                    maskEmail(request.email()), clientIp, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 비밀번호 재설정 OTP 검증 및 비밀번호 변경
     * <p>
     * OTP 인증번호를 검증하고 성공 시 새로운 비밀번호로 변경합니다.
     * </p>
     * 
     * @param request OTP 검증 및 비밀번호 변경 요청 (이메일, OTP, 새 비밀번호 포함)
     * @param httpRequest HTTP 요청 (클라이언트 IP 로깅용)
     * @return 비밀번호 재설정 결과
     */
    @PostMapping("/password/verify-and-reset")
    public ResponseEntity<ApiResponse<Object>> verifyOtpAndResetPassword(
            @Valid @RequestBody PasswordResetVerifyRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        log.info("비밀번호 재설정 OTP 검증 요청: email={}, clientIp={}", maskEmail(request.email()), clientIp);
        
        try {
            passwordResetService.verifyOtpAndReset(request.email(), request.otp(), request.newPassword());
            
            log.info("비밀번호 재설정 완료: email={}, clientIp={}", maskEmail(request.email()), clientIp);
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyMap(), "비밀번호가 성공적으로 재설정되었습니다."));
            
        } catch (CustomException e) {
            log.warn("비밀번호 재설정 OTP 검증 실패: email={}, clientIp={}, error={}", 
                    maskEmail(request.email()), clientIp, e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 재설정 OTP 검증 중 예외 발생: email={}, clientIp={}", 
                    maskEmail(request.email()), clientIp, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 비밀번호 재설정 OTP 재전송
     * <p>
     * 비밀번호 재설정용 OTP 인증번호를 다시 발송합니다.
     * </p>
     * 
     * @param request OTP 재전송 요청 (이메일 포함)
     * @param httpRequest HTTP 요청 (클라이언트 IP 로깅용)
     * @return OTP 재전송 결과
     */
    @PostMapping("/password/resend-otp")
    public ResponseEntity<ApiResponse<Object>> resendPasswordResetOtp(
            @Valid @RequestBody ResendOtpRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        log.info("비밀번호 재설정 OTP 재전송 요청: email={}, clientIp={}", maskEmail(request.email()), clientIp);
        
        try {
            passwordResetService.resendResetOtp(request.email());
            
            log.info("비밀번호 재설정 OTP 재전송 완료: email={}, clientIp={}", maskEmail(request.email()), clientIp);
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyMap(), "인증번호를 다시 발송했습니다."));
            
        } catch (CustomException e) {
            log.warn("비밀번호 재설정 OTP 재전송 실패: email={}, clientIp={}, error={}", 
                    maskEmail(request.email()), clientIp, e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 재설정 OTP 재전송 중 예외 발생: email={}, clientIp={}", 
                    maskEmail(request.email()), clientIp, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }



    /**
     * 클라이언트 IP 주소 추출 (프록시 고려)
     * <p>
     * X-Forwarded-For, X-Real-IP 헤더를 확인하여 실제 클라이언트 IP를 추출합니다.
     * </p>
     * 
     * @param request HTTP 요청
     * @return 클라이언트 IP 주소
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
     * <p>
     * 로그에 기록되는 User-Agent 정보를 마스킹하여 개인정보를 보호합니다.
     * </p>
     * 
     * @param userAgent 원본 User-Agent 문자열
     * @return 마스킹된 User-Agent 문자열
     */
    private String maskUserAgent(String userAgent) {
        if (userAgent == null || userAgent.length() < 20) {
            return "****";
        }
        return userAgent.substring(0, 10) + "****" + userAgent.substring(userAgent.length() - 6);
    }

    /**
     * State 값 마스킹 처리 (보안)
     * <p>
     * OAuth2 state 파라미터를 마스킹하여 로그에 안전하게 기록합니다.
     * </p>
     * 
     * @param state 원본 state 값
     * @return 마스킹된 state 값
     */
    private String maskState(String state) {
        if (state == null || state.length() < 8) {
            return "****";
        }
        return state.substring(0, 4) + "****" + state.substring(state.length() - 4);
    }


    /**
     * 이메일 마스킹 처리 (보안)
     * <p>
     * 이메일 주소를 마스킹하여 로그에 안전하게 기록합니다.
     * </p>
     * 
     * @param email 원본 이메일 주소
     * @return 마스킹된 이메일 주소
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