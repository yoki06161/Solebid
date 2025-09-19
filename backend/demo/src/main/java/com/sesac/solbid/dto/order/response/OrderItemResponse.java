package com.sesac.solbid.dto.order.response;

import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.ProductImage;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record OrderItemResponse(
        String name,
        String image,
        BigDecimal price) {

    public static OrderItemResponse from(Product product, BigDecimal finalPrice) {
        List<ProductImage> images = Optional
                .ofNullable(product.getProductImages())
                .orElse(Collections.emptyList());

        String imageUrl = images
                .stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .or(() -> images.stream().findFirst())
                .map(ProductImage::getFilePath)
                .orElse(null);

        return new OrderItemResponse(
                product.getName(),
                imageUrl,
                finalPrice);
    }
}
