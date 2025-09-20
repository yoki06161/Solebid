package com.sesac.solbid.dto.bid.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public record BidResponse(
        Long bidId,
        Long productId,
        String productName,
        String productImageUrl,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        BigDecimal winningAmount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime bidTime,
        String productBrand,
        String productCategory,
        Integer productSize) {
}