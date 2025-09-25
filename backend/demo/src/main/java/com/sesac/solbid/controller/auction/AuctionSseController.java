package com.sesac.solbid.controller.auction;

import com.sesac.solbid.service.auction.AuctionSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
public class AuctionSseController {
    private final AuctionSseService sse;

    /**
     * 경매 실시간 스트림 구독
     * GET /api/auctions/{auctionId}/stream
     *
     * @header JWT (필요 시 인증 기반 스트림 제어 가능)
     * @pathVariable        auctionId (스트림 구독 대상 경매 ID)
     * @return              SseEmitter (Server-Sent Events 스트림)
     *                      - 실시간 입찰 이벤트, 알림 등을 전송
     *                      - Content-Type: text/event-stream
     *
     * @status 200 OK                스트림 연결 성공
     * @status 404 Not Found         해당 경매 없음
     * @status 503 Service Unavailable  스트림 연결 불가 또는 서버 자원 부족
     * */
    @GetMapping(value = "/{auctionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable Long auctionId) {
        return sse.subscribe(auctionId);
    }
}
