package com.sesac.solbid.service;

import com.sesac.solbid.dto.product.response.ProductResponse;

import java.util.List;

public interface WishService {

    void addWish(Long userId, Long productId);

    void removeWish(Long userId, Long productId);

    List<ProductResponse> getWishes(Long userId);
}
