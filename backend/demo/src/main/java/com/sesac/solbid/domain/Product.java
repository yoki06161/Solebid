package com.sesac.solbid.domain;

import com.sesac.solbid.domain.baseentity.BaseEntity;
import com.sesac.solbid.domain.enums.ProductBrand;
import com.sesac.solbid.domain.enums.ProductCategory;
import com.sesac.solbid.domain.enums.ProductCondition;
import com.sesac.solbid.domain.enums.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    // 판매자
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "seller_id", nullable = false)
    // private User seller;

    @OneToMany(mappedBy = "product")
    private List<AuctionEvent> auctionEvents = new ArrayList<>();

    //이미지
    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("sortOrder ASC") // 오름차순 정렬
    private final List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    List<WishList> wishLists = new ArrayList<>();

    // 상품 메타
    @Enumerated(EnumType.STRING)
    @Column(name = "product_category", nullable = false)
    private ProductCategory productCategory;

    //상품 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false)
    private ProductStatus productStatus = ProductStatus.AVAILABLE;

    //상품 컨디션
    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", nullable = false)
    private ProductCondition productCondition;

    //상품 브랜드
    @Enumerated(EnumType.STRING)
    @Column(name = "product_brand", nullable = false)
    private ProductBrand productBrand;

    //사이즈
    @Column(name = "size", nullable = false)
    @Min(220)
    @Max(320)
    private int size; //mm 기준

    // 등록명
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    //상품 상세설명
    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    // 크롤링 메타
    @Column(name = "model_code", length = 60)
    private String modelCode;

    @Column(name = "colorway", length = 120)
    private String colorway;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Builder
    public Product(
            List<AuctionEvent> auctionEvents,
            ProductCategory productCategory,
            ProductStatus productStatus,
            ProductCondition productCondition,
            ProductBrand productBrand,
            int size,
            String name,
            String description,
            String modelCode,
            String colorway,
            LocalDate releaseDate
    ) {
        this.auctionEvents = auctionEvents;
        this.productCategory = productCategory;
        this.productStatus = (productStatus == null ? ProductStatus.AVAILABLE : productStatus);
        this.productCondition = productCondition;
        this.productBrand = productBrand;
        this.size = size;
        this.name = name;
        this.description = description;
        this.modelCode = modelCode;
        this.colorway = colorway;
        this.releaseDate = releaseDate;
    }

    // 연관관계 편의 메서드
    public void addImage(ProductImage image) {
        image.setProduct(this);
        this.productImages.add(image);
    }
}