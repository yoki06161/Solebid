package com.sesac.solbid.service.auction;

import java.time.LocalDateTime;

public interface AuctionSettlementService {
    /** 종료 시점이 지난 LIVE 경매를 종료하고, 주문 생성/결제까지 처리 */
    void finalizeIfDueTx(Long auctionEventId, LocalDateTime now);
}
