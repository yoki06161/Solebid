package com.sesac.solbid.service.notification;

import com.sesac.solbid.domain.Notification;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.repository.NotificationRepository;
import com.sesac.solbid.repository.UserRepository;
import com.sesac.solbid.service.sse.SsePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sesac.solbid.config.RedisPubSubConfig.CH_USER_NOTIFY;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationReadServiceImpl implements NotificationReadService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final SsePublisher publisher;

    /**미열람 알림 개수 반환*/
    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        User u = userRepo.findById(userId).orElseThrow();
        return notificationRepo.countByUserAndIsReadFalse(u);
    }

    /**알림 목록 최신순 페이징 조회*/
    @Override
    @Transactional(readOnly = true)
    public Page<Notification> list(Long userId, Pageable pageable) {
        User u = userRepo.findById(userId).orElseThrow();
        return notificationRepo.findByUserOrderByCreateAtDesc(u, pageable);
    }

    /**특정 알림을 읽음 처리 후 뱃지 카운트 갱신 브로드캐스트*/
    @Override
    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationRepo.findById(notificationId).orElseThrow();
        if (!n.getUser().getUserId().equals(userId)) throw new IllegalArgumentException("NOT_OWNER");
        if (Boolean.FALSE.equals(n.getIsRead())) {
            n.setIsRead(true);
            notificationRepo.save(n);
            broadcastBadge(userId);
        }
    }

    /**사용자의 최근 200개 알림을 모두 읽음 처리*/
    @Override
    @Transactional
    public void markAllRead(Long userId) {
        // 간단 구현: 최근 200개만 처리
        var page = list(userId, Pageable.ofSize(200));
        page.forEach(n -> { if (Boolean.FALSE.equals(n.getIsRead())) n.setIsRead(true); });
        notificationRepo.saveAll(page.getContent());
        broadcastBadge(userId);
    }

    /**미열람 알림 개수를 SSE로 전송*/
    private void broadcastBadge(Long userId) {
        long count = unreadCount(userId);
        publisher.publish(CH_USER_NOTIFY, new NotificationReadService.BadgePayload(userId, count));
    }
}
