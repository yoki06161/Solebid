package com.sesac.solbid.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class UserNotificationSseService {

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 0L;

    /**SSE 연결을 생성·등록, 기본 이벤트 전송*/
    public SseEmitter subscribe(Long userId) {
        var emitter = new SseEmitter(TIMEOUT);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));

        sendToUsers(Set.of(userId), "hello", Map.of("ts", Instant.now().toString()));
        return emitter;
    }

    /**사용자 집합에게 특정 이벤트와 데이터를 SSE로 전송*/
    public void sendToUsers(Set<Long> userIds, String event, Object data) {
        for (Long uid : userIds) {
            var list = emitters.getOrDefault(uid, new CopyOnWriteArrayList<>());
            for (var emitter : list) {
                try { emitter.send(SseEmitter.event().name(event).data(data)); }
                catch (IOException e) { remove(uid, emitter); }
            }
        }
    }

    /**끊긴 SseEmitter를 제거, 목록이 비면 맵에서 삭제*/
    private void remove(Long userId, SseEmitter emitter) {
        var list = emitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) emitters.remove(userId);
        }
    }
}
