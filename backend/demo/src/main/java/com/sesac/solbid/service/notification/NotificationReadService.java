package com.sesac.solbid.service.notification;

import com.sesac.solbid.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationReadService {
    long unreadCount(Long userId);
    Page<Notification> list(Long userId, Pageable pageable);
    void markRead(Long userId, Long notificationId);
    void markAllRead(Long userId);

    // Redis Pub/Sub 수신 시 타입 안정적으로 역직렬화하기 위한 DTO
    record BadgePayload(Long userId, long unreadCount) {}
}
