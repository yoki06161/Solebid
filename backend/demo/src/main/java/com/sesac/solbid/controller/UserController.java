package com.sesac.solbid.controller;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.ApiResponse;

import com.sesac.solbid.dto.user.request.SignupRequest;
import com.sesac.solbid.dto.user.request.LoginRequest;
import com.sesac.solbid.dto.user.request.NicknameUpdateRequest;
import com.sesac.solbid.dto.user.request.ProfileUpdateRequest;
import com.sesac.solbid.dto.user.request.SensitiveProfileUpdateRequest;
import com.sesac.solbid.dto.user.request.PasswordChangeRequest;
import com.sesac.solbid.dto.user.response.SignupResponse;
import com.sesac.solbid.dto.user.response.LoginResponse;
import com.sesac.solbid.dto.user.response.NicknameAvailabilityResponse;
import com.sesac.solbid.dto.user.response.ProfileUpdateResponse;
import com.sesac.solbid.dto.user.response.PasswordChangeResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ReactivationRequiredException;
import com.sesac.solbid.repository.SocialLoginRepository;
import com.sesac.solbid.service.user.UserService;
import com.sesac.solbid.service.auth.SocialUnlinkService;
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

/**
 * 사용자 관리 컨트롤러
 * <p>
 * 사용자 회원가입, 로그인, 프로필 관리, 회원 탈퇴 등 사용자 관련 기능을 제공하는 컨트롤러입니다.
 * JWT 토큰 기반 인증을 사용하며, 토큰은 HttpOnly 쿠키로 관리됩니다.
 * </p>
 */
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

    /**
     * 회원가입 처리
     * <p>
     * 새로운 사용자의 회원가입을 처리하고 이메일 인증 메일을 발송합니다.
     * </p>
     * 
     * @param requestDto 회원가입 요청 정보 (이메일, 비밀번호, 닉네임, 이름, 전화번호)
     * @return 회원가입 결과 및 사용자 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest requestDto) {
        User user = userService.signup(requestDto);
        SignupResponse responseDto = new SignupResponse(user);

        String message = String.format("회원가입이 완료되었습니다! %s로 인증 이메일을 전송했습니다. " +
                "이메일을 확인하여 계정을 활성화해주세요. (스팸함도 확인해보세요)", 
                user.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDto, message));
    }

    /**
     * 사용자 로그인 처리
     * <p>
     * 이메일과 비밀번호를 통한 사용자 로그인을 처리합니다.
     * 성공 시 JWT 토큰을 HttpOnly 쿠키로 설정합니다.
     * </p>
     * 
     * @param requestDto 로그인 요청 정보 (이메일, 비밀번호)
     * @param response HTTP 응답 (JWT 토큰 쿠키 설정용)
     * @return 로그인 결과 및 사용자 정보
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest requestDto,
            HttpServletResponse response) {
        try {
            LoginResponse responseDto = userService.login(requestDto);

            // HttpOnly 쿠키로 토큰 설정 (CookieUtil 사용)
            cookieUtil.addTokenCookies(
                    response,
                    responseDto.accessToken(), jwtUtil.getAccessTokenValiditySeconds(),
                    responseDto.refreshToken(), jwtUtil.getRefreshTokenValiditySeconds()
            );

            // 응답에서는 토큰 제외하고 사용자 정보만 반환
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", responseDto.userId());
            userData.put("email", responseDto.email());
            userData.put("nickname", responseDto.nickname());
            userData.put("name", responseDto.name()); // 이름 필드 추가
            userData.put("phone", responseDto.phone()); // 전화번호 필드도 추가
            userData.put("userType", responseDto.userType());

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

    /**
     * 이메일 중복 확인
     * <p>
     * 회원가입 시 입력한 이메일 주소의 사용 가능 여부를 확인합니다.
     * </p>
     * 
     * @param email 확인할 이메일 주소
     * @return 이메일 사용 가능 여부
     */
    @GetMapping("/email/available")
    public ResponseEntity<ApiResponse<Map<String, Object>>> isEmailAvailable(
            @RequestParam("email") String email) {
        boolean available = userService.isEmailAvailable(email);
        Map<String, Object> data = new HashMap<>();
        data.put("available", available);
        data.put("email", email);
        
        String message = available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * 닉네임 사용 가능 여부 확인
     * <p>
     * 입력한 닉네임의 중복 여부를 확인합니다.
     * </p>
     * 
     * @param nickname 확인할 닉네임
     * @return 닉네임 사용 가능 여부
     */
    @GetMapping("/nickname/available")
    public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> isNicknameAvailable(
            @RequestParam("nickname") String nickname) {
        boolean available = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ApiResponse.success(new NicknameAvailabilityResponse(available)));
    }

    /**
     * 현재 사용자 닉네임 변경
     * <p>
     * 로그인한 사용자의 닉네임을 변경합니다. JWT 토큰 인증이 필요합니다.
     * </p>
     * 
     * @param request HTTP 요청 (JWT 토큰 쿠키 포함)
     * @param body 닉네임 변경 요청 정보
     * @return 닉네임 변경 결과 및 업데이트된 사용자 정보
     */
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
        User updated = userService.updateNicknameForEmail(email, body.nickname());

        Map<String, Object> data = new HashMap<>();
        data.put("userId", updated.getUserId());
        data.put("email", updated.getEmail());
        data.put("nickname", updated.getNickname());

        return ResponseEntity.ok(ApiResponse.success(data, "닉네임이 설정되었습니다."));
    }

    /**
     * 현재 사용자 일반 프로필 업데이트
     * <p>
     * 로그인한 사용자의 일반적인 프로필 정보(닉네임, 이름)를 업데이트합니다. 
     * JWT 토큰 인증이 필요하며, 추가 인증은 필요하지 않습니다.
     * </p>
     * 
     * @param request HTTP 요청 (JWT 토큰 쿠키 포함)
     * @param body 프로필 업데이트 요청 정보
     * @return 프로필 업데이트 결과 및 업데이트된 사용자 정보
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileUpdateResponse>> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody ProfileUpdateRequest body) {
        Optional<String> accessTokenOpt = getCookieValue(request, "accessToken");
        if (accessTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        
        String email;
        try {
            String token = accessTokenOpt.get();
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
            }
            email = jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
        }
        
        try {
            User updatedUser = userService.updateProfileForEmail(email, body.nickname(), body.name());
            ProfileUpdateResponse responseDto = ProfileUpdateResponse.from(updatedUser);
            
            return ResponseEntity.ok(ApiResponse.success(responseDto, "프로필이 성공적으로 업데이트되었습니다."));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            log.error("프로필 업데이트 처리 중 예외", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 현재 사용자 민감한 프로필 정보 업데이트 (스텝업 인증)
     * <p>
     * 로그인한 사용자의 민감한 정보(이메일, 전화번호)를 업데이트합니다.
     * JWT 토큰 인증과 함께 현재 비밀번호 확인이 필요합니다.
     * </p>
     * 
     * @param request HTTP 요청 (JWT 토큰 쿠키 포함)
     * @param body 민감한 프로필 업데이트 요청 정보
     * @return 프로필 업데이트 결과 및 업데이트된 사용자 정보
     */
    @PutMapping("/profile/sensitive")
    public ResponseEntity<ApiResponse<ProfileUpdateResponse>> updateSensitiveProfile(
            HttpServletRequest request,
            @Valid @RequestBody SensitiveProfileUpdateRequest body) {
        Optional<String> accessTokenOpt = getCookieValue(request, "accessToken");
        if (accessTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        
        String email;
        try {
            String token = accessTokenOpt.get();
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
            }
            email = jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
        }
        
        // 업데이트할 필드가 있는지 확인
        if (!body.hasUpdates()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_INPUT_VALUE", "업데이트할 정보가 없습니다."));
        }
        
        try {
            User updatedUser = userService.updateSensitiveProfileForEmail(
                email, body.currentPassword(), body.email(), body.phone());
            ProfileUpdateResponse responseDto = ProfileUpdateResponse.from(updatedUser);
            
            return ResponseEntity.ok(ApiResponse.success(responseDto, 
                "민감한 정보가 성공적으로 업데이트되었습니다."));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            log.error("민감한 프로필 업데이트 처리 중 예외", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 현재 사용자 비밀번호 변경
     * <p>
     * 로그인한 사용자의 비밀번호를 변경합니다.
     * JWT 토큰 인증과 함께 현재 비밀번호 확인이 필요합니다.
     * 비밀번호 변경 성공 시 모든 기존 세션이 무효화됩니다.
     * </p>
     * 
     * @param request HTTP 요청 (JWT 토큰 쿠키 포함)
     * @param body 비밀번호 변경 요청 정보
     * @param response HTTP 응답 (세션 무효화용)
     * @return 비밀번호 변경 결과
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<PasswordChangeResponse>> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody PasswordChangeRequest body,
            HttpServletResponse response) {
        Optional<String> accessTokenOpt = getCookieValue(request, "accessToken");
        if (accessTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        
        String email;
        try {
            String token = accessTokenOpt.get();
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
            }
            email = jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
        }
        
        // 새 비밀번호와 확인 비밀번호 일치 확인
        if (!body.isPasswordMatching()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_INPUT_VALUE", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."));
        }
        
        // 현재 비밀번호와 새 비밀번호 동일 여부 확인
        if (body.isSameAsCurrentPassword()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("PASSWORD_RESET_SAME_AS_OLD", "새 비밀번호는 현재 비밀번호와 달라야 합니다."));
        }
        
        try {
            userService.changePasswordForEmail(email, body.currentPassword(), body.newPassword());
            
            // 보안을 위해 모든 토큰 쿠키 삭제 (강제 로그아웃)
            cookieUtil.clearTokenCookies(response);
            
            PasswordChangeResponse responseDto = PasswordChangeResponse.success();
            
            return ResponseEntity.ok(ApiResponse.success(responseDto, 
                "비밀번호가 변경되었습니다. 보안을 위해 다시 로그인해주세요."));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 변경 처리 중 예외", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     * <p>
     * JWT 토큰을 통해 현재 로그인한 사용자의 정보를 조회합니다.
     * </p>
     * 
     * @param request HTTP 요청 (JWT 토큰 쿠키 포함)
     * @return 현재 사용자 정보
     */
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
            data.put("name", user.getName()); // 이름 필드 추가
            data.put("phone", user.getPhone()); // 전화번호 필드도 추가
            data.put("userType", user.getUserType() != null ? user.getUserType().name() : null);
            data.put("temperature", user.getTemperature());
            // 연결된 소셜 제공자 정보 포함 (있을 경우)
            socialLoginRepository.findByUser(user).ifPresent(sl -> data.put("socialProvider", sl.getProvider().name()));
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.warn("/api/users/me 처리 실패", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
        }
    }

    /**
     * 회원 탈퇴 처리
     * <p>
     * 현재 로그인한 사용자의 회원 탈퇴를 처리합니다.
     * 연결된 소셜 계정이 있는 경우 연결 해제를 시도하고, JWT 토큰 쿠키를 삭제합니다.
     * </p>
     * 
     * @param request HTTP 요청 (JWT 토큰 쿠키 포함)
     * @param response HTTP 응답 (토큰 쿠키 삭제용)
     * @return 회원 탈퇴 처리 결과
     */
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

    /**
     * 소셜 계정 연결 해제
     * <p>
     * 현재 사용자와 연결된 특정 소셜 계정의 연결을 해제합니다.
     * </p>
     * 
     * @param provider 연결 해제할 소셜 제공자 (google, kakao 등)
     * @param request HTTP 요청 (JWT 토큰 쿠키 포함)
     * @return 소셜 연결 해제 결과
     */
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

    /**
     * 탈퇴 계정 재활성화
     * <p>
     * 탈퇴 처리된 계정을 재활성화하고 즉시 로그인 처리합니다.
     * 재활성화 토큰을 통해 인증을 수행합니다.
     * </p>
     * 
     * @param body 재활성화 요청 (재활성화 토큰 포함)
     * @param response HTTP 응답 (JWT 토큰 쿠키 설정용)
     * @return 재활성화 결과 및 사용자 정보
     */
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
            data.put("name", user.getName()); // 이름 필드 추가
            data.put("phone", user.getPhone()); // 전화번호 필드도 추가
            data.put("userType", user.getUserType() != null ? user.getUserType().name() : null);
            data.put("temperature", user.getTemperature());
            return ResponseEntity.ok(ApiResponse.success(data, "계정이 재활성화되었습니다."));
        } catch (Exception e) {
            log.error("계정 재활성화 처리 중 예외", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.")
            );
        }
    }

    /**
     * 이메일 변경 요청 (1단계: 인증 코드 발송)
     * <p>
     * 새로운 이메일 주소로 인증 코드를 발송합니다.
     * JWT 토큰 인증과 현재 비밀번호 확인이 필요합니다.
     * </p>
     * 
     * @param request HTTP 요청 (JWT 토큰 쿠키 포함)
     * @param body 이메일 변경 요청 정보
     * @return 인증 코드 발송 결과
     */
    @PostMapping("/email/change-request")
    public ResponseEntity<ApiResponse<Map<String, Object>>> requestEmailChange(
            HttpServletRequest request,
            @Valid @RequestBody Map<String, String> body) {
        Optional<String> accessTokenOpt = getCookieValue(request, "accessToken");
        if (accessTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        
        String email;
        try {
            String token = accessTokenOpt.get();
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
            }
            email = jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
        }
        
        String currentPassword = body.get("currentPassword");
        String newEmail = body.get("newEmail");
        
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_INPUT_VALUE", "현재 비밀번호를 입력해주세요."));
        }
        
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_INPUT_VALUE", "새로운 이메일을 입력해주세요."));
        }
        
        try {
            userService.requestEmailChange(email, currentPassword, newEmail);
            
            Map<String, Object> data = new HashMap<>();
            data.put("newEmail", newEmail);
            data.put("message", "새로운 이메일 주소로 인증 코드가 발송되었습니다.");
            
            return ResponseEntity.ok(ApiResponse.success(data, 
                "인증 코드가 발송되었습니다. 새로운 이메일을 확인해주세요."));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            log.error("이메일 변경 요청 처리 중 예외", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 이메일 변경 완료 (2단계: 인증 코드 검증 및 이메일 업데이트)
     * <p>
     * 인증 코드를 검증하고 이메일을 변경합니다.
     * JWT 토큰 인증이 필요합니다.
     * </p>
     * 
     * @param request HTTP 요청 (JWT 토큰 쿠키 포함)
     * @param body 이메일 변경 완료 요청 정보
     * @return 이메일 변경 결과
     */
    @PostMapping("/email/change-confirm")
    public ResponseEntity<ApiResponse<ProfileUpdateResponse>> confirmEmailChange(
            HttpServletRequest request,
            HttpServletResponse response,
            @Valid @RequestBody Map<String, String> body) {
        Optional<String> accessTokenOpt = getCookieValue(request, "accessToken");
        if (accessTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        
        String email;
        try {
            String token = accessTokenOpt.get();
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
            }
            email = jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
        }
        
        String newEmail = body.get("newEmail");
        String verificationCode = body.get("verificationCode");
        
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_INPUT_VALUE", "새로운 이메일을 입력해주세요."));
        }
        
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_INPUT_VALUE", "인증 코드를 입력해주세요."));
        }
        
        try {
            User updatedUser = userService.confirmEmailChange(email, newEmail, verificationCode);
            
            // 이메일 변경 후 새로운 JWT 토큰 발급 (새 이메일로)
            String newAccessToken = jwtUtil.generateToken(updatedUser.getEmail());
            String newRefreshToken = jwtUtil.generateRefreshToken(updatedUser.getEmail());
            
            // 새로운 토큰으로 쿠키 업데이트
            cookieUtil.addTokenCookies(
                response,
                newAccessToken, jwtUtil.getAccessTokenValiditySeconds(),
                newRefreshToken, jwtUtil.getRefreshTokenValiditySeconds()
            );
            
            ProfileUpdateResponse responseDto = ProfileUpdateResponse.from(updatedUser);
            
            return ResponseEntity.ok(ApiResponse.success(responseDto, 
                "이메일이 성공적으로 변경되었습니다. 새로운 토큰이 발급되었습니다."));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus())
                    .body(ApiResponse.error(e.getErrorCode().name(), e.getMessage()));
        } catch (Exception e) {
            log.error("이메일 변경 완료 처리 중 예외", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * HTTP 요청에서 특정 이름의 쿠키 값을 추출합니다.
     * 
     * @param request HTTP 요청
     * @param name 쿠키 이름
     * @return 쿠키 값 (Optional)
     */
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