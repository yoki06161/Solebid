package com.sesac.solbid.controller.cart;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.api.ApiResponse;
import com.sesac.solbid.dto.cart.response.CartResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.service.cart.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    // 장바구니 조회 (낙찰된 상품들)
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartResponse>>> getCartItems(@AuthenticationPrincipal User user) {
        log.info("장바구니 조회 요청 시작");

        if (user == null) {
            log.warn("인증되지 않은 사용자의 장바구니 조회 시도");
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        try {
            log.info("사용자 장바구니 조회: userId={}", user.getUserId());

            List<CartResponse> cartItems = cartService.getCartItems(user.getUserId());
            log.info("장바구니 조회 완료: {} 건", cartItems.size());

            return ResponseEntity.ok(ApiResponse.success(cartItems));
        } catch (CustomException e) {
            log.error("장바구니 조회 중 CustomException 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("장바구니 조회 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }
}