package com.sesac.solbid.service.auction;

import com.sesac.solbid.domain.AuctionEvent;
import com.sesac.solbid.domain.Bid;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.AuctionStatus;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.auction.AuctionEventRepository;

import com.sesac.solbid.repository.bid.BidRepository;
import com.sesac.solbid.service.notification.NotificationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionBidServiceImpl implements AuctionBidService {

    private final AuctionEventRepository auctionRepo;
    private final BidRepository bidRepo;
    private final AuctionSseService sse;
    private final EntityManager em;
    private final NotificationService notificationService;

    /** 비관락으로 충돌을 DB 단계에서 직렬화하므로 재시도 루프는 생략 가능 */
    @Transactional
    @Override
    public void placeBidWithRetry(Long auctionId, Long userId, BigDecimal amount, String idemKey) {
        placeBidCore(auctionId, userId, amount, idemKey);
    }

    private void placeBidCore(Long auctionId, Long userId, BigDecimal amount, String idemKey) {
        if (idemKey == null || idemKey.isBlank())
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "idempotencyKey is required");
        if (bidRepo.existsByIdempotencyKey(idemKey)) {
            log.info("Duplicate bid suppressed: {}", idemKey);
            return;
        }

        // 비관락으로 조회
        AuctionEvent a = auctionRepo.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        if (a.getStatus() != AuctionStatus.LIVE)
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "경매 상태가 LIVE가 아닙니다.");

        var prevHighestBidder = a.getHighestBidder();
        var prevHighestAmount = a.getHighestBidAmount();

        BigDecimal current = (prevHighestAmount != null ? prevHighestAmount : a.getStartPrice());
        BigDecimal minNext = current.add(a.getTickSize());
        if (amount.compareTo(minNext) < 0)
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "입찰가가 최소 호가보다 낮습니다.");

        User bidderRef = em.getReference(User.class, userId);
        a.applyHighestBid(bidderRef, amount);

        bidRepo.save(Bid.builder()
                .auctionEvent(a)
                .bidder(bidderRef)
                .bidAmount(amount)
                .idempotencyKey(idemKey)
                .build());

        // 스나이핑 연장
        if (a.extendIfSniping(LocalDateTime.now())) {
            // 저장 후 즉시 알림
            sse.send(auctionId, "extended",
                    Map.of("endAt", a.getEndAt(), "extendSeconds", a.getExtendSeconds()));
        }

        // 현재가/버전 브로드캐스트
        sse.send(auctionId, "bid", Map.of(
                "auctionEventId", a.getAuctionEventId(),
                "currentPrice", a.getHighestBidAmount(),
                "version", a.getVersion()
        ));

        // 이전 최고입찰자에게 알림
        if (prevHighestBidder != null && !prevHighestBidder.getUserId().equals(userId)) {
            String currentPriceKr = toKRW(a.getHighestBidAmount());
            String myBidKr = toKRW(prevHighestAmount != null ? prevHighestAmount : a.getStartPrice());
            // a.getProduct() 는 LAZY — 이름 접근 시 쿼리 나가도 OK (락과 무관)
            notificationService.notifyOutbid(prevHighestBidder, auctionId,
                    a.getProduct().getName(), currentPriceKr, myBidKr);
        }
        // 트랜잭션 종료 시점에 flush/commit
    }

    /**금액을 한국 통화 포맷 문자열 변환*/
    private String toKRW(BigDecimal v) {
        return java.text.NumberFormat.getCurrencyInstance(java.util.Locale.KOREA).format(v);
    }
}
