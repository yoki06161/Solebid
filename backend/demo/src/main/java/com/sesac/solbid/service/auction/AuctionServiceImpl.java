package com.sesac.solbid.service.auction;

import com.sesac.solbid.domain.AuctionEvent;
import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.AuctionStatus;
import com.sesac.solbid.dto.auction.request.AuctionCreateRequest;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.auction.AuctionEventRepository;
import com.sesac.solbid.repository.product.ProductRepository;
import com.sesac.solbid.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionServiceImpl implements AuctionService {

    private final ProductRepository productRepository;
    private final AuctionEventRepository auctionEventRepository;
    private final UserRepository userRepository;

    /**
     * 경매 이벤트 생성
     */
    @Override
    @Transactional
    public Long create(Long userId, AuctionCreateRequest req) {
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        if (!product.getSeller().getUserId().equals(seller.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 상품에 대한 권한이 없습니다.");
        }

        boolean exists = auctionEventRepository.existsByProductAndStatusIn(
                product, List.of(AuctionStatus.READY, AuctionStatus.LIVE));
        if (exists) {
            throw new CustomException(ErrorCode.AUCTION_ALREADY_EXISTS);
        }

        LocalDateTime startAt = LocalDateTime.now();          // 즉시 시작
        BigDecimal tickSize = (req.tickSize() != null) ? req.tickSize() : new BigDecimal("1.00");
        int extendSeconds   = (req.extendSeconds() != null) ? req.extendSeconds() : 30;

        // 유효성
        if (req.startPrice() == null || req.startPrice().signum() < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "START_PRICE_INVALID");
        }
        if (tickSize.signum() <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "TICK_SIZE_INVALID");
        }
        if (extendSeconds <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "EXTEND_SECONDS_INVALID");
        }
        if (req.endAt() == null || !req.endAt().isAfter(startAt)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "END_AT_MUST_BE_AFTER_START_AT");
        }
        if (req.buyoutPrice() != null && req.buyoutPrice().compareTo(req.startPrice()) < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "BUYOUT_MUST_BE_GTE_START");
        }

        // 생성
        AuctionEvent event = AuctionEvent.builder()
                .product(product)
                .seller(seller)
                .status(AuctionStatus.LIVE)
                .startPrice(req.startPrice())
                .buyoutPrice(req.buyoutPrice())
                .tickSize(tickSize)
                .startAt(startAt)
                .endAt(req.endAt())
                .extendSeconds(extendSeconds)
                .build();

        auctionEventRepository.save(event);
        log.info("AuctionEvent created: id={} productId={} sellerId={}",
                event.getAuctionEventId(), product.getProductId(), seller.getUserId());

        return event.getAuctionEventId();
    }

}
