package com.sesac.solbid.service.bid;

import java.util.List;

import com.sesac.solbid.dto.bid.response.BidSellingResponse;
import com.sesac.solbid.dto.bid.response.BidWinningResponse;

/**
 * 입찰 관련 서비스 인터페이스
 */
public interface BidService {
    
    /**
     * 사용자의 낙찰 상품 목록 조회
     * @param userId 사용자 ID
     * @return 낙찰 상품 목록
     */
    List<BidWinningResponse> getBidsWinning(Long userId);
    
    /**
     * 판매자의 판매 완료 상품 목록 조회
     * @param sellerId 판매자 ID
     * @return 판매 완료 상품 목록
     */
    List<BidSellingResponse> getBidSelling(Long sellerId);
    
    /**
     * 특정 상품의 최고 입찰자를 낙찰자로 설정
     * @param productId 상품 ID
     */
    void markWinningBidForProduct(Long productId);
}