package com.sesac.solbid.dto.auction.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 경매 카드 리스트 노출용 DTO.
 * 프론트에서 리스트 렌더링에 필요한 최소 정보를 제공합니다.
 */
public record AuctionEventCardResponse(
        Long auctionEventId,
        Long productId,
        String brand,
        String name,
        String category,
        String imageUrl,
        BigDecimal currentBid,
        LocalDateTime endAt,
        int bidders
) {
}
