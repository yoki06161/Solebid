package com.sesac.solbid.service.cart;

import org.springframework.stereotype.Component;

import com.sesac.solbid.domain.Bid;
import com.sesac.solbid.dto.cart.response.CartResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartConverter {

    public CartResponse convertToCartResponse(Bid bid) {
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

            String brandName = "UNKNOWN";
            String categoryName = "UNKNOWN";
            
            try {
                if (product.getProductBrand() != null) {
                    brandName = product.getProductBrand().name();
                }
            } catch (Exception e) {
                log.warn("브랜드 정보 변환 실패: productId={}", product.getProductId(), e);
            }
            
            try {
                if (product.getProductCategory() != null) {
                    categoryName = product.getProductCategory().name();
                }
            } catch (Exception e) {
                log.warn("카테고리 정보 변환 실패: productId={}", product.getProductId(), e);
            }

            return new CartResponse(
                    bid.getBidId(), // cartId로 bidId 사용
                    product.getProductId(),
                    product.getName(),
                    imageUrl,
                    bid.getBidAmount(), // 낙찰 금액을 상품 가격으로 사용
                    brandName,
                    categoryName,
                    Integer.valueOf(product.getSize()), // int를 Integer로 변환
                    1 // 수량은 기본 1개
            );
        } catch (Exception e) {
            log.error("CartResponse 변환 중 오류 발생: bidId={}", bid.getBidId(), e);
            throw new RuntimeException("장바구니 아이템 변환 중 오류가 발생했습니다.", e);
        }
    }
}
