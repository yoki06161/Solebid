package com.sesac.solbid.controller;

import com.sesac.solbid.dto.upload.response.FinalizeImagesResponse;
import com.sesac.solbid.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductFinalizeController {
    private final ProductService productService;

    /**
     * 상품 이미지 최종 확정
     * POST /{id}/finalize-images
     *
     * @param id     상품 ID
     * @param userId 요청자 사용자 ID (X-User-Id 헤더)
     * @return 최종 확정 완료 여부와 상품 ID
     */
    @PostMapping("/{id}/finalize-images")
    public ResponseEntity<FinalizeImagesResponse> finalizeImages(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        productService.finalizeImages(id, userId);
        return ResponseEntity.ok(new FinalizeImagesResponse(id, true));
    }
}
