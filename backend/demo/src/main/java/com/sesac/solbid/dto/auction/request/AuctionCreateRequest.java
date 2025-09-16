package com.sesac.solbid.dto.auction.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionCreateRequest(
        @NotNull Long productId,

        @NotNull @DecimalMin("0.00")
        BigDecimal startPrice,

        @NotNull @Future
        LocalDateTime endAt,

        // 선택값
        @DecimalMin("0.00")
        BigDecimal buyoutPrice,

        @DecimalMin("0.00")
        BigDecimal tickSize,

        // null이면 Service에서 now()+5분 기본값
        LocalDateTime startAt,

        // null이면 Service에서 30초 기본값
        Integer extendSeconds
) {}
