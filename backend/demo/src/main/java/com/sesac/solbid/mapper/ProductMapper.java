package com.sesac.solbid.mapper;

import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.ProductStatus;
import com.sesac.solbid.dto.product.request.ProductCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mappings({
            @Mapping(target = "seller", source = "seller"),
            @Mapping(target = "productCategory", source = "req.category"),
            @Mapping(target = "productStatus", source = "req.status", qualifiedByName = "mapStatus"),
            @Mapping(target = "productCondition", source = "req.condition"),
            @Mapping(target = "productBrand", source = "req.brand"),
            @Mapping(target = "size", source = "req.size"),
            @Mapping(target = "name", source = "req.name"),
            @Mapping(target = "description", source = "req.description"),
            @Mapping(target = "modelCode", source = "req.modelCode"),
            @Mapping(target = "colorway", source = "req.colorway"),
            @Mapping(target = "releaseDate", source = "req.releaseDate")

    })

    Product toEntity(ProductCreateRequest req, User seller);


    @Named("mapStatus")
    default ProductStatus mapStatus(ProductStatus status) {
        return status == null ? ProductStatus.AVAILABLE : status;
    }
}
