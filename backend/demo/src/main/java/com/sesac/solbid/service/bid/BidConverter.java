package com.sesac.solbid.service.bid;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.sesac.solbid.domain.Bid;
import com.sesac.solbid.domain.Product;
import com.sesac.solbid.dto.bid.response.BidSellingResponse;
import com.sesac.solbid.dto.bid.response.BidWinningResponse;
import com.sesac.solbid.repository.bid.BidRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidConverter {

    private final BidRepository bidRepository;

    /**
     * Bid 엔티티를 BidWinningResponse로 변환
     */
    public BidWinningResponse convertToBidWinningResponse(Bid bid) {
        var product = bid.getAuctionEvent().getProduct();

        String imageKey = null;

        try {
            if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
                // 썸네일 이미지 우선 선택
                imageKey = product
                        .getProductImages()
                        .stream()
                        .filter(img -> img.isThumbnail())
                        .findFirst()
                        .map(img -> img.getFilePath())
                        .orElse(product.getProductImages().get(0).getFilePath());
            }
        } catch (Exception e) {
            log.warn("상품 이미지 로드 실패: productId={}", product.getProductId(), e);
        }

        return new BidWinningResponse(
                bid.getBidId(),
                product.getProductId(),
                product.getName(),
                imageKey, // S3 키 전달 (프론트엔드에서 presigned URL로 변환)
                bid.getBidAmount(),
                bid.getBidTime(),
                product.getProductBrand() != null ? product.getProductBrand().name() : "UNKNOWN",
                product.getProductCategory() != null ? product.getProductCategory().name() : "UNKNOWN",
                product.getSize());
    }

    /**
     * Product 엔티티를 BidSellingResponse로 변환 (실제 입찰 데이터 사용)
     */
    public BidSellingResponse convertToBidSellingResponse(Product product) {
        log.info("=== convertToBidSellingResponse 호출됨 ===");
        log.info("Product ID: {}, Name: {}", product.getProductId(), product.getName());

        // 상품의 첫 번째 이미지 S3 키 가져오기 (썸네일 우선)
        String imageKey = null;

        try {
            if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
                // 썸네일 이미지 우선 선택
                imageKey = product
                        .getProductImages()
                        .stream()
                        .filter(img -> img.isThumbnail())
                        .findFirst()
                        .map(img -> img.getFilePath())
                        .orElse(product.getProductImages().get(0).getFilePath());
            }
        } catch (Exception e) {
            log.warn("상품 이미지 로드 실패: productId={}", product.getProductId(), e);
        }

        // 해당 상품의 최고 입찰가 조회
        var topBids = bidRepository.findTopBidsByProductId(product.getProductId());
        log.info("상품 {}의 입찰 조회 결과: {} 건", product.getProductId(), topBids.size());

        BigDecimal soldPrice;
        String buyerName;

        if (!topBids.isEmpty()) {
            // 실제 입찰 데이터 사용
            Bid topBid = topBids.get(0);
            soldPrice = topBid.getBidAmount();
            buyerName = topBid.getBidder().getName();
            log.info("실제 입찰 데이터 사용: bidAmount={}, bidder={}", soldPrice, buyerName);
        } else {
            // 입찰 데이터가 없는 경우 기본값 사용
            soldPrice = new BigDecimal("0");
            buyerName = "입찰자 없음";
            log.warn("입찰 데이터 없음 - 기본값 사용: productId={}", product.getProductId());
        }

        BidSellingResponse response = new BidSellingResponse(
                product.getProductId(),
                product.getName(),
                imageKey, // S3 키 전달 (프론트엔드에서 presigned URL로 변환)
                soldPrice, // 실제 입찰가 또는 0
                product.getUpdatedAt(),
                product.getProductBrand() != null ? product.getProductBrand().name() : "UNKNOWN",
                product.getProductCategory() != null ? product.getProductCategory().name() : "UNKNOWN",
                product.getSize(),
                buyerName); // 실제 입찰자명 또는 "입찰자 없음"

        log.info("생성된 BidSellingResponse: productId={}, soldPrice={}",
                response.productId(), response.soldPrice());

        return response;
    }
}
