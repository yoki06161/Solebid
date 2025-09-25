package com.sesac.solbid.repository.auction;

import com.sesac.solbid.domain.AuctionEvent;
import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.enums.AuctionStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface AuctionEventRepository extends JpaRepository<AuctionEvent, Long> {

    boolean existsByProductAndStatusIn(Product product, Collection<AuctionStatus> statuses);

    /** 상세 조회용(읽기): product까지 즉시 로딩 — 락 없음 */
    @Query("""
           select a
           from AuctionEvent a
           join fetch a.product p
           where a.auctionEventId = :id
           """)
    Optional<AuctionEvent> findDetail(@Param("id") Long id);

    /** 동시성 제어가 필요한 구간(입찰/종료): 비관락(PESSIMISTIC_WRITE)
     *  주의: 여기서는 join fetch 제거 (락 범위 최소화 + 비버전 엔티티 이슈 방지)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AuctionEvent a where a.auctionEventId = :id")
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    Optional<AuctionEvent> findByIdForUpdate(@Param("id") Long id);

}
