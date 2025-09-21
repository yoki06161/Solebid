package com.sesac.solbid.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "bid")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long bidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_event_id", nullable = false)
    private AuctionEvent auctionEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(name = "bid_amount", nullable = false, precision = 38, scale = 2)
    private BigDecimal bidAmount;

    //고유키
    @Column(name = "idempotency_key", nullable = false, length = 64, unique = true)
    private String idempotencyKey;

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;

    @Column(name = "is_winning", nullable = false)
    private Boolean isWinning = false;

    @PrePersist
    void onCreate() {
        if (bidTime == null) bidTime = LocalDateTime.now();
        if (isWinning == null) isWinning = false;
    }

    /**
     * 낙찰 처리
     */
    public void markAsWinning() {
        this.isWinning = true;
    }

    /**
     * 낙찰 취소
     */
    public void unmarkAsWinning() {
        this.isWinning = false;
    }
}
