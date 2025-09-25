package com.sesac.solbid.service.wish;

import java.util.List;

import com.sesac.solbid.dto.product.response.ProductResponse;

public interface WishService {

    void addWish(Long userId, Long productId);

    void removeWish(Long userId, Long productId);

    List<ProductResponse> getWishes(Long userId);
}
