package com.sesac.solbid.dto.wish.response;

/**
 * 위시리스트 액션 응답 DTO
 * 
 * 위시리스트 추가/삭제 성공 시 반환되는 정보를 담는 DTO
 * 
 * 
 * @param wishId    위시리스트 ID (추가 시에만 반환, 삭제 시에는 null)
 * @param productId 상품 ID
 * @param action    수행된 액션 ("ADDED" 또는 "REMOVED")
 * @param message   결과 메시지
 */
public record WishActionResponse(
        Long wishId,
        Long productId,
        String action,
        String message) {
    /**
     * 위시리스트 추가 성공 응답을 생성
     * 
     * @param wishId    생성된 위시리스트 ID
     * @param productId 상품 ID
     * @return 위시리스트 추가 성공 응답
     */
    public static WishActionResponse added(Long wishId, Long productId) {
        return new WishActionResponse(
                wishId,
                productId,
                "ADDED",
                "위시리스트에 추가되었습니다.");
    }

    /**
     * 위시리스트 삭제 성공 응답을 생성
     * 
     * @param productId 상품 ID
     * @return 위시리스트 삭제 성공 응답
     */
    public static WishActionResponse removed(Long productId) {
        return new WishActionResponse(
                null,
                productId,
                "REMOVED",
                "위시리스트에서 삭제되었습니다.");
    }
}