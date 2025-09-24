package com.sesac.solbid.service.auction;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AuctionSseService {
    SseEmitter subscribe(Long auctionId);
    void send(Long auctionId, String event, Object data);
}
