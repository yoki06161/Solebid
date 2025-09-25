package com.sesac.solbid.service.product;

import com.sesac.solbid.domain.ProductImage;
import com.sesac.solbid.repository.product.ProductImageRepository;
import com.sesac.solbid.service.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final S3StorageService s3StorageService;

    /** 서버에서 직접 S3에 업로드하고 public URL을 리턴 (기존 시그니처 유지)
     * -> 현재는 클라이언트에서 직접 S3로 업로드 하기 때문에 서버 이미지 저장은 후에 추가 예정 */
    public String upload(MultipartFile file) throws IOException {
        return s3StorageService.uploadAndReturnPublicUrl(file);
    }

    public void save(ProductImage productImage) {
        productImageRepository.save(productImage);
    }
}
