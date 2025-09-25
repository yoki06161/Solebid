package com.sesac.solbid.repository.product;

import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**상품 상세 조회*/
    List<ProductImage> findByProductOrderBySortOrderAsc(Product product);

}
