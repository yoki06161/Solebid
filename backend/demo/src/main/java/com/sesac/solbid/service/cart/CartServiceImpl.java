package com.sesac.solbid.service.cart;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sesac.solbid.domain.Bid;
import com.sesac.solbid.dto.cart.response.CartResponse;
import com.sesac.solbid.repository.cart.CartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 장바구니 서비스 구현체
 * 낙찰된 상품들을 장바구니로 관리하는 비즈니스 로직 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartConverter cartConverter;

    @Override
    @Transactional(readOnly = true)
    public List<CartResponse> getCartItems(Long userId) {
        log.info("사용자 장바구니 조회 시작: userId={}", userId);

        try {
            // Entity 기반 쿼리 사용 후 변환
            List<Bid> bids = cartRepository.findCartItemsByUserId(userId);
            List<CartResponse> cartItems = bids.stream()
                    .map(cartConverter::convertToCartResponse)
                    .toList();
            
            log.info("장바구니 조회 완료: {} 건", cartItems.size());
            return cartItems;
        } catch (Exception e) {
            log.error("장바구니 조회 중 오류 발생: userId={}", userId, e);
            throw new RuntimeException("장바구니 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CartResponse> getCartItemsWithPaging(Long userId, Pageable pageable) {
        log.info("사용자 장바구니 페이징 조회: userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Bid> bidPage = cartRepository.findCartItemsByUserId(userId, pageable);
            return bidPage.map(cartConverter::convertToCartResponse);
        } catch (Exception e) {
            log.error("장바구니 페이징 조회 중 오류 발생: userId={}", userId, e);
            throw new RuntimeException("장바구니 페이징 조회 중 오류가 발생했습니다.", e);
        }
    }
}