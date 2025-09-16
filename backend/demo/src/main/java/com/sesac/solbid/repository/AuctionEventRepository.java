package com.sesac.solbid.repository;


import com.sesac.solbid.domain.AuctionEvent;
import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface AuctionEventRepository extends JpaRepository<AuctionEvent, Long> {
    boolean existsByProductAndStatusIn(Product product, Collection<AuctionStatus> statuses);
}