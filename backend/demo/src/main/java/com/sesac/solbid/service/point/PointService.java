package com.sesac.solbid.service.point;

import com.sesac.solbid.dto.payment.response.PointSummaryResponse;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PointService {
    /**유저 포인트 단건 조회*/
    PointSummaryResponse getCurrentPoint(Long userId);

    @Transactional
    void capture(Long userId, BigDecimal amount, Long auctionEventId, String description);

    /** 낙찰 즉시 차감 (결제) — 별도 트랜잭션 */
    void captureTx(Long userId, BigDecimal amount, Long auctionEventId, String description);

    /** 환불/충전 — 별도 트랜잭션 */
    void refundTx(Long userId, BigDecimal amount, Long auctionEventId, String description);
}