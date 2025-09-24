package com.sesac.solbid.service.wish;

import com.sesac.solbid.dto.product.response.ProductResponse;

import java.util.List;

public interface WishService {

    WishActionResponse addWish(Long userId, Long productId);

    WishActionResponse removeWish(Long userId, Long productId);

    List<WishResponse> getWishes(Long userId);
}
