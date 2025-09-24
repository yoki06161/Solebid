package com.sesac.solbid.service.auction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class AuctionSseServiceImpl implements AuctionSseService {

    /**경매 ID별 SseEmitter 목록을 동시 관리*/
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 0L;

    /**특정 경매 SSE 연결을 생성·등록, 기본 이벤트 전송*/
    @Override
    public SseEmitter subscribe(Long auctionId) {
        var emitter = new SseEmitter(TIMEOUT);
        emitters.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(auctionId, emitter));
        emitter.onTimeout(() -> remove(auctionId, emitter));
        emitter.onError(e -> remove(auctionId, emitter));

        send(auctionId, "hello", Map.of("ts", Instant.now().toString()));
        return emitter;
    }

    /**특정 경매에 연결된 모든 클라이언트에게 이벤트와 데이터를 SSE로 전송*/
    @Override
    public void send(Long auctionId, String event, Object data) {
        var list = emitters.getOrDefault(auctionId, new CopyOnWriteArrayList<>());
        for (var emitter : list) {
            try { emitter.send(SseEmitter.event().name(event).data(data)); }
            catch (IOException e) { remove(auctionId, emitter); }
        }
    }

    /**끊기거나 에러 난 SseEmitter를 제거하고, 목록이 비면 맵에서 삭제*/
    private void remove(Long auctionId, SseEmitter emitter) {
        var list = emitters.get(auctionId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) emitters.remove(auctionId);
        }
    }
}
