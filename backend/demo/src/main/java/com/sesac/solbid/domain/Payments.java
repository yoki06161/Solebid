package com.sesac.solbid.domain;

import com.sesac.solbid.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_order_id", columnList = "order_id", unique = true),
                @Index(name = "idx_payments_transaction_id", columnList = "transaction_id")
        }
)
public class Payments extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PointTransaction> pointTransactions = new ArrayList<>();

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "payment_method", length = 20, nullable = false)
    private String paymentMethod; // 필요 시 enum으로 변경

    @Column(name = "transaction_id", length = 100, unique = true)
    private String transactionId;

    @Column(length = 30, nullable = false)
    private String provider; // 기본값은 PrePersist에서 설정

    @Column(name = "order_id", length = 100, nullable = false, unique = true)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "converted_point", nullable = false)
    private int convertedPoint;

    @Column(name = "is_charged", nullable = false)
    private boolean charged;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Builder
    private Payments(
            User user,
            int amount,
            String paymentMethod,
            String orderId,
            PaymentStatus paymentStatus,
            Integer convertedPoint,
            Boolean charged,
            LocalDateTime requestedAt
    ) {
        this.user = user;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;                         // null → PrePersist에서 UUID로 대체
        this.paymentStatus = paymentStatus;             // null → PrePersist에서 WAITING
        this.convertedPoint = convertedPoint != null ? convertedPoint : 0;
        this.charged = charged != null && charged;
        this.requestedAt = requestedAt;                 // null → PrePersist에서 now()
        // provider, transactionId, confirmedAt은 상황에 따라 이후 세팅
    }

    @PrePersist
    void prePersist() {
        if (paymentStatus == null) paymentStatus = PaymentStatus.WAITING;
        if (orderId == null || orderId.isBlank()) orderId = UUID.randomUUID().toString();
        if (provider == null || provider.isBlank()) provider = "PORTONE";
        if (requestedAt == null) requestedAt = LocalDateTime.now();
    }

    /** 결제 승인(성공) 처리 */
    public void approve(String transactionId, int convertedPoint) {
        this.transactionId = transactionId;
        this.convertedPoint = convertedPoint;
        this.paymentStatus = PaymentStatus.SUCCESS; // 도메인에 맞게 APPROVED/SUCCESS 선택
        this.charged = true;
        this.confirmedAt = LocalDateTime.now();
    }

    /** 결제 실패 처리 */
    public void fail() {
        this.paymentStatus = PaymentStatus.FAIL;
        this.charged = false;
        this.confirmedAt = LocalDateTime.now();
    }
}
