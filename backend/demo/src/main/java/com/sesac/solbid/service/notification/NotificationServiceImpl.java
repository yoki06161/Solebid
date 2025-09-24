package com.sesac.solbid.service.notification;

import com.sesac.solbid.domain.Notification;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.NotificationType;
import com.sesac.solbid.repository.NotificationRepository;
import com.sesac.solbid.service.sse.SseMessages;
import com.sesac.solbid.service.sse.SsePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static com.sesac.solbid.config.RedisPubSubConfig.CH_USER_NOTIFY;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;
    private final SsePublisher publisher;

    /**입찰에서 밀렸을 때 알림 생성 및 SSE 발행*/
    @Override
    public void notifyOutbid(User receiver, Long auctionEventId,
                             String productName, String currentPriceKr, String myBidKr) {

        // 영속 저장 (보관/마이페이지 용)
        Notification n = new Notification();
        n.setUser(receiver);
        n.setNotificationType(NotificationType.OUTBID);
        n.setTitle("입찰가 갱신 알림");
        n.setContent(productName + " | 현재가 " + currentPriceKr + " / 내 입찰가 " + myBidKr);
        n.setLinkUrl("/auction/" + auctionEventId);
        notificationRepo.save(n);

        // Pub/Sub 발행 (모든 노드가 구독 중 → 각자 로컬 emitter로 전송)
        SseMessages.UserOutbid msg = SseMessages.UserOutbid.builder()
                .userId(receiver.getUserId())
                .auctionEventId(auctionEventId)
                .productName(productName)
                .currentPriceText(currentPriceKr)
                .myBidText(myBidKr)
                .build();

        publisher.publish(CH_USER_NOTIFY, msg);
    }
}
