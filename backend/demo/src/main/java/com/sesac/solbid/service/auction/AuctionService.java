package com.sesac.solbid.service.auction;

import com.sesac.solbid.dto.auction.request.AuctionCreateRequest;

public interface AuctionService {
    Long create(Long userId, AuctionCreateRequest req);
}