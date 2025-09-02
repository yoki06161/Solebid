package com.sesac.solbid.dto.payment.response;

import com.sesac.solbid.domain.Payments;
import com.sesac.solbid.domain.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentRecordItemResponse {
    private Long paymentId;
    private Long userId;
    private String orderId;
    private String transactionId;
    private int amount;
    private String paymentMethod;
    private String provider;
    private PaymentStatus paymentStatus;
    private boolean charged;
    private int convertedPoint;
    private LocalDateTime requestedAt;
    private LocalDateTime confirmedAt;

    // 엔티티 매핑용 — EntityGraph 용
    public static PaymentRecordItemResponse from(Payments p) {
        return PaymentRecordItemResponse.builder()
                .paymentId(p.getPaymentId())
                .userId(p.getUser() != null ? p.getUser().getUserId() : null)
                .orderId(p.getOrderId())
                .transactionId(p.getTransactionId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .provider(p.getProvider())
                .paymentStatus(p.getPaymentStatus())
                .charged(p.isCharged())
                .convertedPoint(p.getConvertedPoint())
                .requestedAt(p.getRequestedAt())
                .confirmedAt(p.getConfirmedAt())
                .build();
    }

    // JPQL 생성자 프로젝션용
    public PaymentRecordItemResponse(Long paymentId, Long userId, String orderId,
                                     String transactionId, int amount, String paymentMethod,
                                     String provider, PaymentStatus paymentStatus,
                                     boolean charged, int convertedPoint,
                                     LocalDateTime requestedAt, LocalDateTime confirmedAt) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.provider = provider;
        this.paymentStatus = paymentStatus;
        this.charged = charged;
        this.convertedPoint = convertedPoint;
        this.requestedAt = requestedAt;
        this.confirmedAt = confirmedAt;
    }
}