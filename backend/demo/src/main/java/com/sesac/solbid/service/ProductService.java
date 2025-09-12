package com.sesac.solbid.service;

import java.util.List;

import com.sesac.solbid.dto.product.request.ProductCreateRequest;
import com.sesac.solbid.dto.product.response.ProductResponse;

public interface ProductService {
    Long create(Long sellerId, ProductCreateRequest req);
    void finalizeImages(Long id, Long userId);
    List<ProductResponse> getProducts();
}
