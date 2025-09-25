package com.sesac.solbid.repository.order;

import com.sesac.solbid.domain.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long> {

    boolean existsByAuctionEvent_AuctionEventId(Long auctionEventId);
}
