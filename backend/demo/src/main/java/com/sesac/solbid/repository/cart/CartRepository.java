package com.sesac.solbid.repository.cart;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sesac.solbid.domain.Bid;
import com.sesac.solbid.dto.cart.response.CartResponse;

/**
 * 장바구니 관련 데이터 접근 Repository
 * 낙찰된 상품들을 장바구니로 관리
 */
@Repository
public interface CartRepository extends JpaRepository<Bid, Long> {

    /**
     * 특정 사용자의 장바구니 아이템 조회 (낙찰된 상품들)
     * 성능 최적화: 첫 번째 이미지만 조회
     */
    @Query("SELECT DISTINCT b FROM Bid b " +
            "JOIN FETCH b.bidder " +
            "JOIN FETCH b.auctionEvent ae " +
            "JOIN FETCH ae.product p " +
            "LEFT JOIN FETCH p.productImages pi " +
            "WHERE b.bidder.userId = :userId " +
            "AND b.isWinning = true " +
            "AND (pi IS NULL OR pi.id = (SELECT MIN(pi2.id) FROM ProductImage pi2 WHERE pi2.product = p)) " +
            "ORDER BY b.bidTime DESC")
    List<Bid> findCartItemsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 장바구니 아이템 조회 (페이징)
     */
    @Query("SELECT DISTINCT b FROM Bid b " +
            "JOIN FETCH b.bidder " +
            "JOIN FETCH b.auctionEvent ae " +
            "JOIN FETCH ae.product p " +
            "LEFT JOIN FETCH p.productImages pi " +
            "WHERE b.bidder.userId = :userId " +
            "AND b.isWinning = true " +
            "AND (pi IS NULL OR pi.id = (SELECT MIN(pi2.id) FROM ProductImage pi2 WHERE pi2.product = p)) " +
            "ORDER BY b.bidTime DESC")
    Page<Bid> findCartItemsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 장바구니용 최적화된 쿼리 - DTO 프로젝션 사용
     * 필요한 필드만 조회하여 메모리 사용량과 네트워크 트래픽 최소화
     */
    @Query("SELECT new com.sesac.solbid.dto.cart.response.CartResponse(" +
            "b.bidId, " +
            "p.productId, " +
            "p.name, " +
            "(SELECT MIN(pi.filePath) FROM ProductImage pi WHERE pi.product = p), " +
            "b.bidAmount, " +
            "COALESCE(CAST(p.productBrand AS string), 'UNKNOWN'), " +
            "COALESCE(CAST(p.productCategory AS string), 'UNKNOWN'), " +
            "p.size, " +
            "1) " +
            "FROM Bid b " +
            "JOIN b.auctionEvent ae " +
            "JOIN ae.product p " +
            "WHERE b.bidder.userId = :userId " +
            "AND b.isWinning = true " +
            "ORDER BY b.bidTime DESC")
    List<CartResponse> findCartResponsesByUserId(@Param("userId") Long userId);
}