package com.sesac.solbid.repository.product;

import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    
    /**
     * 특정 판매자의 특정 상태 상품 목록을 최신 순으로 조회
     */
    List<Product> findBySellerAndProductStatusOrderByUpdatedAtDesc(User seller, ProductStatus status);
    
    /**
     * 상품 목록 조회 (기본 정렬)
     */
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 입찰 수 기준 정렬된 상품 목록 조회
     */
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN p.auctionEvents ae " +
           "GROUP BY p.productId " +
           "ORDER BY COALESCE(MAX(ae.viewCount), 0) DESC, p.createdAt DESC")
    List<Product> findAllOrderByBidCount(Pageable pageable);
    
    /**
     * 상품 이미지를 페치 조인으로 조회 (N+1 문제 해결)
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.productImages " +
           "WHERE p.productId IN :productIds " +
           "ORDER BY p.createdAt DESC")
    List<Product> findByProductIdsWithImages(@Param("productIds") List<Long> productIds);
    
    /**
     * 상품 경매 정보를 페치 조인으로 조회 (N+1 문제 해결)
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.auctionEvents " +
           "WHERE p.productId IN :productIds " +
           "ORDER BY p.createdAt DESC")
    List<Product> findByProductIdsWithAuctions(@Param("productIds") List<Long> productIds);
    
    /**
     * 검색 시 연관 엔티티 함께 조회
     */
    @Query("SELECT p FROM Product p " +
           "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.createdAt DESC")
    List<Product> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(@Param("keyword") String keyword);
}
