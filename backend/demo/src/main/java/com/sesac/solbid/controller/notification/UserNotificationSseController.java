package com.sesac.solbid.controller.notification;

import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.service.notification.UserNotificationSseService;
import com.sesac.solbid.service.stream.StreamTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stream")
public class UserNotificationSseController {

    private final UserNotificationSseService userSse;
    private final StreamTokenService tokenService;


    /* 토큰 미사용
     * 사용자 알림 스트림 구독
     * GET /api/stream/notifications
     *
     * @header JWT (필수, 사용자 식별용)
     * @return              SseEmitter (Server-Sent Events 스트림)
     *                      - 사용자별 실시간 알림 이벤트 전송
     *                      - Content-Type: text/event-stream
     *
     * @status 200 OK                스트림 연결 성공
     * @status 503 Service Unavailable  스트림 연결 불가 또는 서버 자원 부족

    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter notifications(@AuthenticationPrincipal User authUser) {
        return userSse.subscribe(authUser.getUserId());
    }
    */

    /**
     * 사용자 알림 SSE 스트림 구독
     * GET /api/stream/notifications
     *
     * @param token 발급된 스트림 토큰 (유효성 검증 후 사용자 식별에 사용)
     * @return      {@link SseEmitter} 객체.
     *              서버에서 발생하는 알림 이벤트를 실시간으로 전달
     */
    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter notifications(@RequestParam("token") String token) {
        Long userId = tokenService.validate(token);
        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_STREAM_TOKEN);
        }
        return userSse.subscribe(userId);
    }




}
