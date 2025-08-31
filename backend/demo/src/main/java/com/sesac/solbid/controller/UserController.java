package com.sesac.solbid.controller;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.ApiResponse;

import com.sesac.solbid.dto.user.request.SignupRequest;
import com.sesac.solbid.dto.user.request.LoginRequest;
import com.sesac.solbid.dto.user.request.NicknameUpdateRequest;
import com.sesac.solbid.dto.user.response.SignupResponse;
import com.sesac.solbid.dto.user.response.LoginResponse;
import com.sesac.solbid.dto.user.response.NicknameAvailabilityResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ReactivationRequiredException;
import com.sesac.solbid.repository.SocialLoginRepository;
import com.sesac.solbid.service.UserService;
import com.sesac.solbid.service.SocialUnlinkService;
import com.sesac.solbid.util.JwtUtil;
import com.sesac.solbid.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final SocialUnlinkService socialUnlinkService;
    private final SocialLoginRepository socialLoginRepository;
    private final CookieUtil cookieUtil;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest requestDto) {
        User user = userService.signup(requestDto);
        SignupResponse responseDto = new SignupResponse(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest requestDto,
            HttpServletResponse response) {
        try {
            LoginResponse responseDto = userService.login(requestDto);

            // HttpOnly 쿠키로 토큰 설정 (CookieUtil 사용)
            cookieUtil.addTokenCookies(
                    response,
                    responseDto.getAccessToken(), jwtUtil.getAccessTokenValiditySeconds(),
                    responseDto.getRefreshToken(), jwtUtil.getRefreshTokenValiditySeconds()
            );

            // 응답에서는 토큰 제외하고 사용자 정보만 반환
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", responseDto.getUserId());
            userData.put("email", responseDto.getEmail());
            userData.put("nickname", responseDto.getNickname());
            userData.put("userType", responseDto.getUserType());

            return ResponseEntity.ok(ApiResponse.success(userData));
        } catch (ReactivationRequiredException e) {
            // 탈퇴 계정 - 재활성화 토큰 발급
            String token = jwtUtil.generateReactivationToken(e.getEmail(), 600); // 10분 유효
            Map<String, Object> data = new HashMap<>();
            data.put("reactivationToken", token);
            data.put("email", e.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(data, "WITHDRAWN_USER", "회원 탈퇴 처리된 계정입니다. 계정을 다시 활성화하시겠습니까?"));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus()).body(
                    ApiResponse.error(e.getErrorCode().name(), e.getMessage())
            );
        } catch (Exception e) {
            log.error("/api/users/login 처리 중 예외", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.")
            );
        }
    }

    // 닉네임 가용성 확인
    @GetMapping("/nickname/available")
    public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> isNicknameAvailable(
            @RequestParam("nickname") String nickname) {
        boolean available = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ApiResponse.success(NicknameAvailabilityResponse.builder()
                .available(available)
                .build()));
    }

    // 현재 사용자 닉네임 설정 (accessToken 쿠키 필요)
    @PostMapping("/nickname")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateNickname(
            HttpServletRequest request,
            @Valid @RequestBody NicknameUpdateRequest body) {
        Optional<String> accessTokenOpt = getCookieValue(request, "accessToken");
        if (accessTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        String email;
        try {
            email = jwtUtil.getUsernameFromToken(accessTokenOpt.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
        }
        User updated = userService.updateNicknameForEmail(email, body.getNickname());

        Map<String, Object> data = new HashMap<>();
        data.put("userId", updated.getUserId());
        data.put("email", updated.getEmail());
        data.put("nickname", updated.getNickname());

        return ResponseEntity.ok(ApiResponse.success(data, "닉네임이 설정되었습니다."));
    }

    // 현재 사용자 조회 (accessToken 쿠키 필요)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(HttpServletRequest request) {
        Optional<String> accessTokenOpt = getCookieValue(request, "accessToken");
        if (accessTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        try {
            String token = accessTokenOpt.get();
            // 토큰 유효성(서명/���료) 검증 추가
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
            }
            String email = jwtUtil.getUsernameFromToken(token);
            User user = userService.getByEmail(email);
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getUserId());
            data.put("email", user.getEmail());
            data.put("nickname", user.getNickname());
            data.put("userType", user.getUserType() != null ? user.getUserType().name() : null);
            // 연결된 소셜 제공자 정보 포함 (있을 경우)
            socialLoginRepository.findByUser(user).ifPresent(sl -> data.put("socialProvider", sl.getProvider().name()));
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.warn("/api/users/me 처리 실패", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
        }
    }

    // 회원 탈퇴 (accessToken 쿠키 필요)
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> withdraw(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> accessTokenOpt = getCookieValue(request, "accessToken");
        if (accessTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        String token = accessTokenOpt.get();
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
        }
        try {
            String email = jwtUtil.getUsernameFromToken(token);
            // 연결된 소셜 계정이 있으면 먼저 연결 해제 시도 (카카오 unlink 등)
            User user = userService.getByEmail(email);
            try {
                socialUnlinkService.unlinkAllForUser(user);
            } catch (Exception ex) {
                log.warn("탈퇴 전 소셜 unlink 실패(무시): {}", ex.getMessage());
            }
            userService.withdrawByEmail(email);
            // 토큰 쿠키 삭제로 자동 로그아웃 처리
            cookieUtil.clearTokenCookies(response);
            return ResponseEntity.ok(ApiResponse.success(new HashMap<>(), "회원탈퇴가 완료되었습니다."));
        } catch (Exception e) {
            log.error("회원탈퇴 처리 중 예외", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    // 소셜 연결 해제 (accessToken 쿠키 필요)
    @DeleteMapping("/me/social/{provider}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unlinkSocial(
            @PathVariable String provider,
            HttpServletRequest request
    ) {
        Optional<String> accessTokenOpt = getCookieValue(request, "accessToken");
        if (accessTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        String token = accessTokenOpt.get();
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
        }
        try {
            String email = jwtUtil.getUsernameFromToken(token);
            User user = userService.getByEmail(email);
            Map<String, Object> data = socialUnlinkService.unlinkForUserAndProvider(user, provider);
            String msg = "소셜 연결이 해제되었습니다.";
            if ("Google".equals(data.get("provider")) && Boolean.FALSE.equals(data.get("remoteRevoked"))) {
                msg = "연결이 해제되었습니다. Google 계정 보안 설정에서 앱 권한 철회를 완료하세요.";
            }
            return ResponseEntity.ok(ApiResponse.success(data, msg));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(ApiResponse.error("BAD_REQUEST", "지원하지 않는 소셜 제공자입니다."));
        } catch (Exception e) {
            log.warn("소셜 연결 해제 실패: provider={}", provider, e);
            return ResponseEntity.badRequest().body(ApiResponse.error("BAD_REQUEST", "소셜 연결 해제에 실패했습니다."));
        }
    }

    // 계정 재활성화 (토큰 기반) + 즉시 로그인 처리
    @PostMapping("/reactivate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reactivate(@RequestBody Map<String, String> body,
                                                                       HttpServletResponse response) {
        String token = body != null ? body.get("token") : null;
        if (token == null || token.isBlank() || !jwtUtil.validateToken(token) || !jwtUtil.isReactivationToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 요청입니다."));
        }
        String email = jwtUtil.extractEmailFromReactivationToken(token);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 요청입니다."));
        }
        try {
            User user = userService.reactivateByEmail(email);
            // 즉시 로그인 쿠키 설정 (CookieUtil 사용)
            String at = jwtUtil.generateToken(user.getEmail());
            String rt = jwtUtil.generateRefreshToken(user.getEmail());
            cookieUtil.addTokenCookies(response, at, jwtUtil.getAccessTokenValiditySeconds(), rt, jwtUtil.getRefreshTokenValiditySeconds());
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getUserId());
            data.put("email", user.getEmail());
            data.put("nickname", user.getNickname());
            data.put("userType", user.getUserType() != null ? user.getUserType().name() : null);
            return ResponseEntity.ok(ApiResponse.success(data, "계정이 재활성화되었습니다."));
        } catch (Exception e) {
            log.error("계정 재활성화 처리 중 예외", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.")
            );
        }
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return Optional.ofNullable(c.getValue());
            }
        }
        return Optional.empty();
    }

}