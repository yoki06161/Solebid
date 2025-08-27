package com.sesac.solbid.controller;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.UserDto;
import com.sesac.solbid.dto.ApiResponse;

import com.sesac.solbid.service.UserService;
import com.sesac.solbid.util.JwtUtil;
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

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserDto.SignupResponse>> signup(@Valid @RequestBody UserDto.SignupRequest requestDto) {
        User user = userService.signup(requestDto);
        UserDto.SignupResponse responseDto = new UserDto.SignupResponse(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody UserDto.LoginRequest requestDto,
            HttpServletResponse response) {
        
        UserDto.LoginResponse responseDto = userService.login(requestDto);

        // HttpOnly 쿠키로 토큰 설정
        setTokenCookies(response, responseDto.getAccessToken(), responseDto.getRefreshToken());

        // 응답에서는 토큰 제외하고 사용자 정보만 반환
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", responseDto.getUserId());
        userData.put("email", responseDto.getEmail());
        userData.put("nickname", responseDto.getNickname());
        userData.put("userType", responseDto.getUserType());

        return ResponseEntity.ok(ApiResponse.success(userData));
    }

    // 닉네임 가용성 확인
    @GetMapping("/nickname/available")
    public ResponseEntity<ApiResponse<UserDto.NicknameAvailabilityResponse>> isNicknameAvailable(
            @RequestParam("nickname") String nickname) {
        boolean available = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ApiResponse.success(UserDto.NicknameAvailabilityResponse.builder()
                .available(available)
                .build()));
    }

    // 현재 사용자 닉네임 설정 (accessToken 쿠키 필요)
    @PostMapping("/nickname")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateNickname(
            HttpServletRequest request,
            @Valid @RequestBody UserDto.NicknameUpdateRequest body) {
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
            // 토큰 유효성(서명/만료) 검증 추가
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
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.warn("/api/users/me 처리 실패", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
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

    /**
     * HttpOnly 쿠키로 토큰 설정
     */
    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token 쿠키 설정
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);  // JavaScript 접근 차단
        accessTokenCookie.setSecure(false);   // 개발환경에서는 false, 운영환경에서는 true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(3600);    // 1시간
        response.addCookie(accessTokenCookie);
        
        // Refresh Token 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);  // 개발환경에서는 false, 운영환경에서는 true
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(86400);  // 24시간
        response.addCookie(refreshTokenCookie);
    }

}