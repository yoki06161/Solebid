package com.sesac.solbid.domain;

import com.sesac.solbid.domain.baseentity.BaseEntity;
import com.sesac.solbid.domain.enums.AuctionStatus;
import com.sesac.solbid.domain.enums.EventEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name="auction_event")
public class AuctionEvent  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_event_id")
    private Long  auctionEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 경매 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private AuctionStatus status = AuctionStatus.READY; //기본값

    // 시작가/즉시구매가/호가단위
    @Column(name = "start_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "buyout_price", precision = 19, scale = 2)
    private BigDecimal buyoutPrice;

    @Column(name = "tick_size", nullable = false, precision = 19, scale = 2)
    private BigDecimal tickSize  = new BigDecimal("1.00"); //기본값

    // 일정
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    // 스나이핑 연장(초)
    @Column(name = "extend_seconds")
    private Integer extendSeconds;

    // 최고가/입찰자 캐시
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highest_bidder_id")
    private User highestBidder;

    @Column(name = "highest_bid_amount", precision = 19, scale = 2)
    private BigDecimal highestBidAmount;

    // 동시성 제어
    @Version
    @Column(name = "version")
    private Long version;

    // 부가
    @Column(name = "view_count", nullable = false)
    @ColumnDefault("0")
    private Integer viewCount = 0;

    @Column(name = "is_blind", nullable = false)
    @ColumnDefault("false")
    private Boolean isBlind = false;
}
