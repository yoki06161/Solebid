package com.sesac.solbid.domain;

import com.sesac.solbid.domain.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    // 경매의 판매자 (권한/리스트업/원샷 취소용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

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
    private final Integer viewCount = 0;

    @Column(name = "is_blind", nullable = false)
    @ColumnDefault("false")
    private final Boolean isBlind = false;

    //  생성/비즈니스 메서드
    @Builder
    public AuctionEvent(Product product, User seller, AuctionStatus status,
                        BigDecimal startPrice, BigDecimal buyoutPrice, BigDecimal tickSize,
                        LocalDateTime startAt, LocalDateTime endAt, Integer extendSeconds,
                        User highestBidder, BigDecimal highestBidAmount) {
        this.product = product;
        this.seller = seller;
        this.status = status == null ? AuctionStatus.READY : status;
        this.startPrice = normalize(startPrice);
        this.buyoutPrice = buyoutPrice == null ? null : normalize(buyoutPrice);
        this.tickSize = tickSize == null ? new BigDecimal("1.00") : normalize(tickSize);
        this.startAt = startAt;
        this.endAt = endAt;
        this.extendSeconds = extendSeconds == null ? 30 : extendSeconds;
        this.highestBidder = highestBidder;
        this.highestBidAmount = highestBidAmount == null ? this.startPrice : normalize(highestBidAmount);
    }

    private BigDecimal normalize(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    // READY에서만 판매자가 취소 가능
    public void cancelBy(Long sellerId) {
        if (!this.seller.getUserId().equals(sellerId)) {
            throw new IllegalStateException("ONLY_SELLER_CAN_CANCEL");
        }
        if (this.status != AuctionStatus.READY) {
            throw new IllegalStateException("AUCTION_NOT_READY");
        }
        this.status = AuctionStatus.CANCELED;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.seller = product.getSeller(); // 항상 상품의 seller로 맞춤
        }
    }

    @PrePersist @PreUpdate
    private void syncSeller() {
        if (this.product != null) {
            this.seller = this.product.getSeller();
        }
    }

    /** 최고 입찰 갱신 (정규화 포함) */
    public void applyHighestBid(User bidder, BigDecimal amount) {
        if (bidder == null) throw new IllegalArgumentException("bidder is null");
        if (amount == null) throw new IllegalArgumentException("amount is null");
        this.highestBidder = bidder;
        this.highestBidAmount = normalize(amount);
    }

    /** 스나이핑 연장: 남은 시간이 extendSeconds 이하이면 endAt += extendSeconds */
    public boolean extendIfSniping(LocalDateTime now) {
        if (this.extendSeconds == null || this.endAt == null) return false;
        long remain = java.time.Duration.between(now, this.endAt).getSeconds();
        if (remain <= this.extendSeconds) {
            this.endAt = this.endAt.plusSeconds(this.extendSeconds);
            return true;
        }
        return false;
    }

    /** 경매 종료(도메인 규칙에 맞는 진입점) */
    public void markEnded() {
        this.status = AuctionStatus.ENDED;
    }

}
