package com.sesac.solbid.service.cart;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sesac.solbid.dto.cart.response.CartResponse;

/**
 * 장바구니 서비스 인터페이스
 * 낙찰된 상품들을 장바구니로 관리하는 비즈니스 로직 정의
 */
public interface CartService {

    /**
     * 사용자의 장바구니 아이템 조회
     * 
     * @param userId 사용자 ID
     * @return 장바구니 아이템 목록
     */
    List<CartResponse> getCartItems(Long userId);

    /**
     * 사용자의 장바구니 아이템 페이징 조회
     * 
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 장바구니 아이템
     */
    Page<CartResponse> getCartItemsWithPaging(Long userId, Pageable pageable);
}
