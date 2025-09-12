package com.sesac.solbid.controller;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.ApiResponse;
import com.sesac.solbid.dto.product.response.ProductResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.service.WishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishes")
@Log4j2
public class WishController {

    private final WishService wishService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getWishes(@AuthenticationPrincipal User user) {
        List<ProductResponse> wishes = Optional
                .ofNullable(user)
                .map(u -> wishService.getWishes(u.getUserId()))
                .orElse(Collections.emptyList());

        return ResponseEntity.ok(ApiResponse.success(wishes));
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addWish(@AuthenticationPrincipal User user, @PathVariable Long productId) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        wishService.addWish(user.getUserId(), productId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeWish(@AuthenticationPrincipal User user, @PathVariable Long productId) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        wishService.removeWish(user.getUserId(), productId);
    }
}
