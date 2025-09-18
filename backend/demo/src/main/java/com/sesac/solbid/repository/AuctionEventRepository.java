package com.sesac.solbid.repository;


import com.sesac.solbid.domain.AuctionEvent;
import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.enums.AuctionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;

public interface AuctionEventRepository extends JpaRepository<AuctionEvent, Long> {
    boolean existsByProductAndStatusIn(Product product, Collection<AuctionStatus> statuses);

    /**상품 상세 조회*/
    // 상세 조회용: product까지 즉시 로딩
    @Query("""
           select a
           from AuctionEvent a
           join fetch a.product p
           where a.auctionEventId = :id
           """)
    Optional<AuctionEvent> findDetail(Long id);

    // (추후 입찰용) 낙관락
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select a from AuctionEvent a join fetch a.product p where a.auctionEventId = :id")
    Optional<AuctionEvent> findByIdForUpdateOptimistic(Long id);
}