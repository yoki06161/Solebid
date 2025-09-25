package com.sesac.solbid.service.auction;

import com.sesac.solbid.domain.AuctionEvent;
import com.sesac.solbid.domain.OrderInfo;
import com.sesac.solbid.domain.enums.AuctionStatus;
import com.sesac.solbid.domain.enums.DeliveryStatus;
import com.sesac.solbid.domain.enums.PaymentStatus;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.auction.AuctionEventRepository;
import com.sesac.solbid.repository.order.OrderInfoRepository;
import com.sesac.solbid.service.point.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSettlementServiceImpl implements AuctionSettlementService {

    private final AuctionEventRepository auctionRepo;
    private final OrderInfoRepository orderRepo;
    private final PointService pointService;
    private final AuctionSseService sse;

    /**특정 경매 종료 시점 확인 후 종료 처리*/
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeIfDueTx(Long auctionEventId, LocalDateTime now) {
        AuctionEvent a = auctionRepo.findByIdForUpdate(auctionEventId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        if (a.getStatus() != AuctionStatus.LIVE) return;
        if (a.getEndAt() == null || now.isBefore(a.getEndAt())) return;

        // 낙찰자 없음 → 유찰 종료
        if (a.getHighestBidder() == null || a.getHighestBidAmount() == null) {
            a.markEnded();
            sse.send(a.getAuctionEventId(), "status", Map.of("status", "ENDED"));
            return;
        }

        // 이미 주문 있으면 상태만 종료
        if (orderRepo.existsByAuctionEvent_AuctionEventId(a.getAuctionEventId())) {
            a.markEnded();
            sse.send(a.getAuctionEventId(), "status", Map.of("status", "ENDED"));
            return;
        }

        // 자동 결제 (부족/오류 시 WAITING)
        PaymentStatus paymentStatus;
        try {
            pointService.captureTx(
                    a.getHighestBidder().getUserId(),
                    a.getHighestBidAmount(),
                    a.getAuctionEventId(),
                    "경매 #" + a.getAuctionEventId() + " 낙찰 결제"
            );
            paymentStatus = PaymentStatus.SUCCESS;
        } catch (CustomException e) {
            log.warn("Auto-capture failed: auction={}, user={}, code={}, msg={}",
                    a.getAuctionEventId(), a.getHighestBidder().getUserId(), e.getErrorCode(), e.getMessage());
            paymentStatus = PaymentStatus.WAITING;
        } catch (Exception e) {
            log.warn("Auto-capture failed: auction={}, user={}, msg={}",
                    a.getAuctionEventId(), a.getHighestBidder().getUserId(), e.getMessage());
            paymentStatus = PaymentStatus.WAITING;
        }

        // 주문 생성
        var order = OrderInfo.builder()
                .auctionEvent(a)
                .winner(a.getHighestBidder())
                .seller(a.getSeller()).finalPrice(a.getHighestBidAmount())
                .paymentStatus(paymentStatus)
                .deliveryStatus(DeliveryStatus.PREPARING)
                .deliveryAddress("TBD")
                .build();
        try {
            orderRepo.save(order);
        } catch (DataIntegrityViolationException dup) {
            log.warn("Order already exists for auction {}", a.getAuctionEventId());
        }

        // 경매 종료
        a.markEnded();

        //스트림 알림
        sse.send(a.getAuctionEventId(), "status", Map.of("status", "ENDED"));
    }
}
