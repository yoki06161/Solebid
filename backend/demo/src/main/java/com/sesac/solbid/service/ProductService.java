package com.sesac.solbid.service;

import com.sesac.solbid.dto.product.request.ProductCreateRequest;
import com.sesac.solbid.dto.product.response.ProductResponse;

import java.util.List;

public interface ProductService {

    Long create(Long sellerId, ProductCreateRequest req);

    /**상품 등록 이미지 최종 확정*/
    void finalizeImages(Long id, Long userId);

    /**경매가 하나라도 있으면 판매자 변경 불가*/
    void changeSeller(Long productId, Long newSellerId);

    List<ProductResponse> getProducts(String sortBy, Integer limit);

    List<ProductResponse> searchProducts(String keyword);
}
