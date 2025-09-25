package com.sesac.solbid.controller.stream;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.service.stream.StreamTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stream")
public class StreamTokenController {

    private final StreamTokenService tokenService;

    /**
     * SSE 스트림 연결을 위한 토큰 발급
     * POST /api/stream/token
     *
     * @param me 인증된 사용자 객체
     * @return   발급된 토큰을 담은 Map
     */
    @PostMapping("/token")
    public Map<String, String> issue(@AuthenticationPrincipal User me) {
        if (me == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        String token = tokenService.issue(me.getUserId());
        return Map.of("token", token);
    }


    /**
     * 토큰 강제 만료 처리
     * DELETE /api/auth/token/{token}
     *
     * @param token 만료시킬 대상 액세스 토큰 값
     * @return {"success": true} 형태의 성공 여부 응답
     */
    @DeleteMapping("/token/{token}")
    public Map<String, Object> revoke(@PathVariable String token) {
        tokenService.revoke(token);
        return Map.of("success", true);
    }
}
