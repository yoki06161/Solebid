package com.sesac.solbid.dto.auction.response;

import com.sesac.solbid.domain.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AuctionDetailResponse(
        // Auction
        Long auctionEventId,
        AuctionStatus status,
        BigDecimal startPrice,
        BigDecimal buyoutPrice,
        BigDecimal currentPrice,
        BigDecimal tickSize,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer extendSeconds,
        Integer viewCount,
        Boolean isBlind,
        long version,

        // Product summary
        ProductSummary product
) {
    public record ProductSummary(
            Long productId,
            String name,
            String brand,       // ENUM 문자열
            String category,    // ENUM 문자열
            Integer size,
            String condition,   // ENUM 문자열
            String modelCode,
            String colorway,
            LocalDate releaseDate,
            List<ProductImage> images
    ) {}

    public record ProductImage(
            String filePath,
            Boolean isThumbnail,
            Integer sortOrder
    ) {}
}
