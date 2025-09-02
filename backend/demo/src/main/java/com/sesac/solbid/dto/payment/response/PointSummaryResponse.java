package com.sesac.solbid.dto.payment.response;


import lombok.Builder;

@Builder
public record PointSummaryResponse(
        Long userId,
        long currentPoint,
        // 정수 포인트 기준 후에 balanceAfter가 소수점이 필요한 비즈니스면 long 대신 String 또는 BigDecimal로 변경
        String updatedAt
) {}