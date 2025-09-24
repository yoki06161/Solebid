package com.sesac.solbid.service.auction;

import java.math.BigDecimal;

public interface AuctionBidService {
    void placeBidWithRetry(Long auctionId, Long userId, BigDecimal amount, String idempotencyKey);
}
