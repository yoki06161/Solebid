package com.sesac.solbid.service.cart;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sesac.solbid.domain.Bid;
import com.sesac.solbid.dto.cart.response.CartResponse;
import com.sesac.solbid.repository.bid.BidRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final BidRepository bidRepository;

    @Transactional(readOnly = true)
    public List<CartResponse> getCartItems(Long userId) {
        log.info("사용자 장바구니 조회 시작: userId={}", userId);

        try {
            // 낙찰된 상품들을 장바구니 아이템으로 조회
            List<Bid> winningBids = bidRepository.findByBidderUserIdAndIsWinningTrueOrderByBidTimeDesc(userId);
            log.info("낙찰된 상품 조회 완료: {} 건", winningBids.size());

            List<CartResponse> cartItems = winningBids
                    .stream()
                    .map(this::convertToCartResponse)
                    .collect(Collectors.toList());
            log.info("CartResponse 변환 완료: {} 건", cartItems.size());

            return cartItems;
        } catch (Exception e) {
            log.error("장바구니 조회 중 오류 발생: userId={}", userId, e);
            throw new RuntimeException("장바구니 조회 중 오류가 발생했습니다.", e);
        }
    }

    private CartResponse convertToCartResponse(Bid bid) {
        try {
            var product = bid.getAuctionEvent().getProduct();
            log.debug("상품 정보 변환 시작: productId={}", product.getProductId());

            String imageUrl = null;
            try {
                if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
                    imageUrl = product.getProductImages().get(0).getFilePath();
                }
            } catch (Exception e) {
                log.warn("상품 이미지 로드 실패: productId={}", product.getProductId(), e);
            }

            return new CartResponse(
                    bid.getBidId(), // cartId로 bidId 사용
                    product.getProductId(),
                    product.getName(),
                    imageUrl,
                    bid.getBidAmount(), // 낙찰 금액을 상품 가격으로 사용
                    product.getProductBrand() != null ? product.getProductBrand().name() : "UNKNOWN",
                    product.getProductCategory() != null ? product.getProductCategory().name() : "UNKNOWN",
                    Integer.valueOf(product.getSize()), // int를 Integer로 변환
                    1 // 수량은 기본 1개
            );
        } catch (Exception e) {
            log.error("CartResponse 변환 중 오류 발생: bidId={}", bid.getBidId(), e);
            throw new RuntimeException("장바구니 아이템 변환 중 오류가 발생했습니다.", e);
        }
    }
}
