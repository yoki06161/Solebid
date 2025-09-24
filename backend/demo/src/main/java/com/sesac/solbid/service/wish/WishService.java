package com.sesac.solbid.service.wish;

import java.util.List;

import com.sesac.solbid.dto.wish.response.WishActionResponse;
import com.sesac.solbid.dto.wish.response.WishResponse;

public interface WishService {

    WishActionResponse addWish(Long userId, Long productId);

    WishActionResponse removeWish(Long userId, Long productId);

    List<WishResponse> getWishes(Long userId);
}
