package com.sesac.solbid.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.service.notification.NotificationReadService;
import com.sesac.solbid.service.notification.UserNotificationSseService;
import com.sesac.solbid.service.sse.SseMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "sse.redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisPubSubConfig {

    /**사용자 알림용 Redis Pub/Sub 채널 이름*/
    public static final String CH_USER_NOTIFY = "sse:user:notify";

    private final StringRedisTemplate redis;
    private final UserNotificationSseService userSse;
    private final ObjectMapper om = new ObjectMapper();

    /**RedisMessageListenerContainer 초기화 후 CH_USER_NOTIFY 구독 시작*/
    @PostConstruct
    public void initListener() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redis.getConnectionFactory());

        container.addMessageListener(userNotifyListener(), new PatternTopic(CH_USER_NOTIFY));

        container.afterPropertiesSet();
        container.start();
        log.info("[RedisPubSub] listeners started.");
    }

    /**Redis 메시지 수신 시 JSON 파싱*/
    private MessageListener userNotifyListener() {
        return (message, pattern) -> {
            try {
                String json = new String(message.getBody());

                if (json.contains("\"currentPriceText\"")) {
                    // Outbid 알림
                    SseMessages.UserOutbid m = om.readValue(json, SseMessages.UserOutbid.class);
                    userSse.sendToUsers(
                            java.util.Set.of(m.getUserId()),
                            "outbid",
                            java.util.Map.of(
                                    "auctionEventId", m.getAuctionEventId(),
                                    "title", "입찰가 갱신 알림",
                                    "productName", m.getProductName(),
                                    "currentPriceText", m.getCurrentPriceText(),
                                    "myBidText", m.getMyBidText(),
                                    "link", "/auction/" + m.getAuctionEventId()
                            )
                    );
                } else if (json.contains("\"unreadCount\"")) {
                    // 배지 카운트
                    NotificationReadService.BadgePayload m =
                            om.readValue(json, NotificationReadService.BadgePayload.class);

                    userSse.sendToUsers(
                            java.util.Set.of(m.userId()),
                            "badge",
                            java.util.Map.of("unreadCount", m.unreadCount())
                    );
                }
            } catch (Exception e) {
                log.error("User notify listener error", e);
            }
        };
    }
}
