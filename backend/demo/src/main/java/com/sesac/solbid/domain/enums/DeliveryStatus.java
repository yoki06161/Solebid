package com.sesac.solbid.domain.enums;

public enum DeliveryStatus {
    /**
     * 결제 완료 후, 판매자가 상품 발송을 준비 중인 상태
     */
    PREPARING,

    /**
     * 상품이 인계되어 고객에게 이동 중인 상태
     */
    SHIPPED,

    /**
     * 고객의 주소지에 상품 배송을 완료한 상태
     */
    DELIVERED,

    /**
     * 상품 발송 전에 주문이 취소된 상태
     */
    CANCELED
}
