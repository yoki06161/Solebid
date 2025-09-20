package com.sesac.solbid.repository.bid;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sesac.solbid.domain.Bid;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    /**
     * 특정 사용자의 낙찰 내역 조회 (isWinning = true)
     */
    @Query("SELECT DISTINCT b FROM Bid b " +
            "JOIN FETCH b.bidder " +
            "JOIN FETCH b.auctionEvent ae " +
            "JOIN FETCH ae.product p " +
            "LEFT JOIN FETCH p.productImages pi " +
            "WHERE b.bidder.userId = :userId " +
            "AND b.isWinning = true " +
            "ORDER BY b.bidTime DESC")
    List<Bid> findByBidderUserIdAndIsWinningTrueOrderByBidTimeDesc(@Param("userId") Long userId);

    /**
     * 특정 사용자의 낙찰 내역 조회 (페이징)
     */
    @Query("SELECT DISTINCT b FROM Bid b " +
            "JOIN FETCH b.bidder " +
            "JOIN FETCH b.auctionEvent ae " +
            "JOIN FETCH ae.product p " +
            "LEFT JOIN FETCH p.productImages pi " +
            "WHERE b.bidder.userId = :userId " +
            "AND b.isWinning = true " +
            "ORDER BY b.bidTime DESC")
    Page<Bid> findWinningBidsByUserId(@Param("userId") Long userId, Pageable pageable);
}