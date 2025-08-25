package com.sesac.solbid.domain;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.baseentity.BaseEntity;
import com.sesac.solbid.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payments extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false) // Long type에 length 삭제
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "payments")
    private List<PointTransaction> pointTransaction = new ArrayList<>();

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "payment_method", length = 20, nullable = false)
    private String paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(length = 30, nullable = false) //멱등/식별 키 생성
    private String provider = "PORTONE";

    @Column(name = "order_id", length = 100, nullable = false) //portone orderId 생성
    private String orderId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @ColumnDefault("0")
    @Column(name = "converted_point", nullable = false)
    private int convertedPoint;

    @Column(name = "is_charged", nullable = false)
    @ColumnDefault("false")
    private Boolean isCharged;

    private LocalDateTime requestedAt;

    private LocalDateTime confirmedAt;


    // ---- 기본값 런타임에서 확정 ----
    @PrePersist
    public void prePersist() {
        if (paymentStatus == null) paymentStatus = PaymentStatus.WAITING;
        if (orderId == null || orderId.isBlank()) orderId = UUID.randomUUID().toString();
        if (isCharged == null) isCharged = false;
        if (requestedAt == null) requestedAt = LocalDateTime.now();
        if (provider == null || provider.isBlank()) provider = "PORTONE";
    }
}