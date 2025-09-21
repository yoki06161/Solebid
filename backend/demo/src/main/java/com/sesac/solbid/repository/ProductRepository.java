package com.sesac.solbid.repository;

import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    
    /**
     * 특정 판매자의 특정 상태 상품 목록을 최신 순으로 조회
     */
    List<Product> findBySellerAndProductStatusOrderByUpdatedAtDesc(User seller, ProductStatus status);
}
