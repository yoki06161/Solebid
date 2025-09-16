package com.sesac.solbid.service;

import com.sesac.solbid.dto.product.request.ProductCreateRequest;
import com.sesac.solbid.dto.product.response.ProductResponse;

import java.util.List;

public interface ProductService {

    Long create(Long sellerId, ProductCreateRequest req);

    void finalizeImages(Long id, Long userId);

    List<ProductResponse> getProducts(String sortBy, Integer limit);

    List<ProductResponse> searchProducts(String keyword);
}
