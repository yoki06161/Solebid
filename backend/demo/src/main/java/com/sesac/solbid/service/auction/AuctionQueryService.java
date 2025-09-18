package com.sesac.solbid.service.auction;

import com.sesac.solbid.dto.auction.response.AuctionDetailResponse;

public interface AuctionQueryService {
    /**상품 상세 조회*/
    AuctionDetailResponse getAuctionDetail(Long auctionId);
}
