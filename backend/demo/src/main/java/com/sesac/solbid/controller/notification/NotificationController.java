// com/sesac/solbid/controller/NotificationController.java
package com.sesac.solbid.controller.notification;

import com.sesac.solbid.domain.Notification;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.service.notification.NotificationReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationReadService service;

    /**
     * 읽지 않은 알림 개수를 반환
     * GET /api/notifications/unread-count
     *
     * @param me 인증된 사용자 객체
     * @return   읽지 않은 알림 개수를 담은 Map
     */
    @GetMapping("/unread-count")
    public Map<String, Object> unreadCount(@AuthenticationPrincipal User me) {
        return Map.of("count", service.unreadCount(me.getUserId()));
    }

    /**
     * 사용자 알림 목록을 페이징 형태로 반환
     * GET /api/notifications
     *
     * @param me   인증된 사용자 객체
     * @param page 요청할 페이지 번호 (기본값 0)
     * @param size 페이지당 항목 수 (기본값 20)
     * @return     페이징 처리된 알림 {@link Page}
     */
    @GetMapping
    public Page<Notification> list(@AuthenticationPrincipal User me,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return service.list(me.getUserId(), PageRequest.of(page, size));
    }

    /**
     * 특정 알림을 읽음 처리
     * POST /api/notifications/{id}/read
     *
     * @param me 인증된 사용자 객체
     * @param id 읽음 처리할 알림 ID
     * @return   성공 여부를 담은 Map
     */
    @PostMapping("/{id}/read")
    public Map<String, Object> markRead(@AuthenticationPrincipal User me, @PathVariable Long id) {
        service.markRead(me.getUserId(), id);
        return Map.of("success", true);
    }

    /**
     * 사용자의 모든 알림을 읽음 처리
     * POST /api/notifications/read-all
     *
     * @param me 인증된 사용자 객체
     * @return   성공 여부를 담은 Map
     */
    @PostMapping("/read-all")
    public Map<String, Object> markAllRead(@AuthenticationPrincipal User me) {
        service.markAllRead(me.getUserId());
        return Map.of("success", true);
    }
}
