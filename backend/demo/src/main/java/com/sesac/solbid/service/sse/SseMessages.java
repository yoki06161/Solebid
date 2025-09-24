package com.sesac.solbid.service.sse;

import lombok.*;

import java.math.BigDecimal;

/**메시지 모델*/
public class SseMessages {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserOutbid {
        private Long userId;           // 수신자
        private Long auctionEventId;
        private String productName;
        private String currentPriceText;
        private String myBidText;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuctionEventBroadcast {
        private Long auctionId;
        private String type;           // "bid" | "extended" | "status"
        private BigDecimal currentPrice;
        private String endAt;
        private Integer extendSeconds;
        private Long version;
    }
}
