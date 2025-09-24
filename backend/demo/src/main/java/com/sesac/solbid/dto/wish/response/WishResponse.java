package com.sesac.solbid.dto.wish.response;

import java.time.LocalDateTime;

import com.sesac.solbid.domain.Wish;
import com.sesac.solbid.dto.product.response.ProductResponse;

/**
 * 위시리스트 응답 DTO
 * 
 * 위시리스트 조회 시 반환되는 정보를 담는 DTO
 * 위시리스트 ID와 상품 정보를 포함
 * 
 * @param wishId    위시리스트 고유 ID
 * @param userId    사용자 ID
 * @param product   상품 정보
 * @param createdAt 위시리스트 추가 시간
 */
public record WishResponse(
        Long wishId,
        Long userId,
        ProductResponse product,
        LocalDateTime createdAt) {
    /**
     * Wish 엔티티로부터 WishResponse를 생성
     * 
     * @param wish 위시리스트 엔티티
     * @return 생성된 WishResponse 객체
     */
    public static WishResponse from(Wish wish) {
        return new WishResponse(
                wish.getId(),
                wish.getUser().getUserId(),
                ProductResponse.fromEntity(wish.getProduct()),
                null // BaseEntity가 있다면 createdAt 필드 추가 가능
        );
    }

    /**
     * Wish 엔티티와 생성 시간으로부터 WishResponse를 생성
     * 
     * @param wish      위시리스트 엔티티
     * @param createdAt 생성 시간
     * @return 생성된 WishResponse 객체
     */
    public static WishResponse from(Wish wish, LocalDateTime createdAt) {
        return new WishResponse(
                wish.getId(),
                wish.getUser().getUserId(),
                ProductResponse.fromEntity(wish.getProduct()),
                createdAt);
    }
}