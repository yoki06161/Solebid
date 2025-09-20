package com.sesac.solbid.dto.cart.response;

import java.math.BigDecimal;

public record CartResponse(
        Long cartId,
        Long productId,
        String productName,
        String productImageUrl,
        BigDecimal productPrice,
        String productBrand,
        String productCategory,
        Integer productSize,
        Integer quantity
) {
}