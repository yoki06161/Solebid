package com.sesac.solbid.controller;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.upload.response.FinalizeImagesResponse;
import com.sesac.solbid.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductFinalizeController {
    private final ProductService productService;

    /**
     * 상품 이미지 최종 확정
     * POST /{id}/finalize-images
     *
     * @param id 상품 ID
     * @param authUser 인증된 사용자 (JWT 인증 객체)
     * @return 최종 확정 완료 여부와 상품 ID
     */
    @PostMapping("/{id}/finalize-images")
    public ResponseEntity<FinalizeImagesResponse> finalizeImages(
            @PathVariable Long id,
            @AuthenticationPrincipal User authUser
    ) {
        Long userId = authUser.getUserId();
        productService.finalizeImages(id, userId);
        return ResponseEntity.ok(new FinalizeImagesResponse(id, true));
    }

}
