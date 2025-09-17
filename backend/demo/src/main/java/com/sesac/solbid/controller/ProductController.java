package com.sesac.solbid.controller;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.ApiResponse;
import com.sesac.solbid.dto.product.request.ProductCreateRequest;
import com.sesac.solbid.dto.product.response.ProductCreateResponse;
import com.sesac.solbid.dto.product.response.ProductResponse;
import com.sesac.solbid.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 등록
     * * POST /api/products
     *
     * @param authUser 인증된 사용자 정보 (로그인된 회원)
     * @param req      상품 등록 요청 본문 (상품명, 이미지 목록 등)
     * @return 생성된 상품의 ID를 담은 응답 (201 Created)
     *
     * 사용자가 전달한 상품 정보(ProductCreateRequest)를 기반으로
     * 새로운 상품을 생성한다.
     * 생성 과정에서 인증 사용자 ID를 함께 기록하며,
     * 등록된 상품의 고유 ID를 반환한다.
     * */
    @PostMapping public ResponseEntity<?> create(
            @AuthenticationPrincipal User authUser,
            @Validated @RequestBody ProductCreateRequest req
    ) {
        Long userId = authUser.getUserId();
        log.info("POST /api/products by userId={} name={} images={}", userId, req.name(),
                req.images() != null ? req.images().size() : 0);

        Long id = productService.create(userId, req);
        log.info("Product created: id={} by userId={}", id, userId);

        return ResponseEntity.status(201).body(new ProductCreateResponse(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Integer limit
    ) {
        List<ProductResponse> products = productService.getProducts(sortBy, limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam("keyword") String keyword
    ) {
        List<ProductResponse> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
