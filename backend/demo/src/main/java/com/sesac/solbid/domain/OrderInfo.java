package com.sesac.solbid.domain;

import com.sesac.solbid.domain.enums.DeliveryStatus;
import com.sesac.solbid.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "order_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_event_id")
    private AuctionEvent auctionEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "final_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 50)
    private DeliveryStatus deliveryStatus;

    @Lob
    @Column(name = "delivery_address", nullable = false, length = 65535)
    private String deliveryAddress;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Builder
    public OrderInfo(AuctionEvent auctionEvent, User winner, User seller, BigDecimal finalPrice,
                     PaymentStatus paymentStatus, String deliveryAddress, DeliveryStatus deliveryStatus ) {
        this.auctionEvent = auctionEvent;
        this.winner = winner;
        this.seller = seller;
        this.finalPrice = finalPrice;
        this.paymentStatus = paymentStatus;
        this.deliveryStatus = deliveryStatus;
        this.deliveryAddress = deliveryAddress;
        this.orderDate = LocalDateTime.now();
        this.paymentDate = LocalDateTime.now();


    }

    public void markAsPaid() {
        if (this.paymentStatus == PaymentStatus.WAITING) {
            this.paymentStatus = PaymentStatus.SUCCESS;
            this.paymentDate = LocalDateTime.now();
        }
    }

    public void startShipping(String trackingNumber) {
        if (this.paymentStatus != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("결제가 완료되지 않은 주문은 배송을 시작할 수 없습니다.");
        }
        this.trackingNumber = trackingNumber;
        this.deliveryStatus = DeliveryStatus.SHIPPED;
        this.deliveryDate = LocalDateTime.now();
    }

    public void cancel() {
        if (this.deliveryStatus == DeliveryStatus.SHIPPED || this.deliveryStatus == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("이미 배송이 시작된 주문은 취소할 수 없습니다.");
        }
        this.paymentStatus = PaymentStatus.FAIL;
        this.deliveryStatus = DeliveryStatus.CANCELED;
    }
}
