package com.sesac.solbid.controller;

import com.sesac.solbid.dto.ApiResponse;
import com.sesac.solbid.dto.product.request.ProductCreateRequest;
import com.sesac.solbid.dto.product.response.ProductCreateResponse;
import com.sesac.solbid.dto.product.response.ProductResponse;
import com.sesac.solbid.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    /*
     * 상품 등록
     * POST /api/products
     *
     * @param userId 인증된 사용자 ID (JWT 필터에서 추출됨)
     * @param req    상품 등록 요청 DTO
     * @return 201 Created + { productId }
     *
     * 검증 규칙:
     * - size 220~320
     * - images ≤ 5, 썸네일 ≤ 1
     * - sortOrder 중복 불가
     * - filePath는 "products/" prefix
     * */

    /* 테스트 코드
    @PostMapping
    public ResponseEntity<?> create(@RequestAttribute("userId") Long userId,  // or @AuthUser 커스텀 리졸버
                                    @Validated @RequestBody ProductCreateRequest req) {
        Long id = productService.create(userId, req);
        return ResponseEntity.status(201).body(new ProductCreateResponse(id));
    }

    private record ProductCreateResponse(Long productId) {}
    */

    // JWT 필터 붙이고 다시 @RequestAttribute("userId")로 원복
    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-User-Id") Long userId, @Validated @RequestBody ProductCreateRequest req) {
        log.info("POST /api/products by userId={} name={} images={}", userId, req.name(), req.images() != null ? req.images().size() : 0);
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
