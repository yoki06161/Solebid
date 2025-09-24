package com.sesac.solbid.controller.wish;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.ApiResponse;
import com.sesac.solbid.dto.wish.request.WishRequest;
import com.sesac.solbid.dto.wish.response.WishActionResponse;
import com.sesac.solbid.dto.wish.response.WishResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.service.wish.WishService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishes")
@Log4j2
public class WishController {

    private final WishService wishService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishResponse>>> getWishes(@AuthenticationPrincipal User user) {
        List<WishResponse> wishes = Optional
                .ofNullable(user)
                .map(u -> wishService.getWishes(u.getUserId()))
                .orElse(Collections.emptyList());

        return ResponseEntity.ok(ApiResponse.success(wishes));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<WishActionResponse>> addWish(@AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        WishActionResponse response = wishService.addWish(user.getUserId(), productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WishActionResponse>> addWishWithBody(@AuthenticationPrincipal User user,
            @Valid @RequestBody WishRequest request) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        WishActionResponse response = wishService.addWish(user.getUserId(), request.productId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<WishActionResponse>> removeWish(@AuthenticationPrincipal User user,
            @PathVariable Long productId) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        WishActionResponse response = wishService.removeWish(user.getUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
