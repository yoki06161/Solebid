package com.sesac.solbid.service.product;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.ProductImage;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.product.request.ProductCreateRequest;
import com.sesac.solbid.dto.product.response.ProductResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.infra.S3ObjectMover;
import com.sesac.solbid.infra.S3ObjectVerifier;
import com.sesac.solbid.mapper.ProductImageMapper;
import com.sesac.solbid.mapper.ProductMapper;
import com.sesac.solbid.repository.product.ProductRepository;
import com.sesac.solbid.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private static final String SORT_BY_OPTION = "bidCount";

    private static final String TMP_PREFIX = "products/tmp/"; // tmp: 보안 강화

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper; // MapStruct
    private final ProductImageMapper productImageMapper; // MapStruct

    // S3 검증/이동 컴포넌트
    private final S3ObjectVerifier s3ObjectVerifier; // S3 HEAD 존재 확인
    private final S3ObjectMover s3ObjectMover; // tmp → 최종 경로 이동(Copy+Delete)

    /**
     * 상품 등록
     * - 검증: 썸네일 ≤ 1, sortOrder 중복 금지, S3 key prefix + S3 존재 확인
     * - 처리: DTO → 엔티티 매핑(MapStruct) 후 저장
     *
     * @throws CustomException 인증/검증 실패 시
     */
    @Override
    @Transactional
    public Long create(Long sellerId, ProductCreateRequest req) {

        // 판매자 조회
        User seller = userRepository
                .findById(sellerId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        // 이미지 검증
        validateImages(req);

        // 매핑 (Product 엔티티 생성)
        Product product = productMapper.toEntity(req, seller);

        // 이미지 엔티티 연결
        for (var dto : req.images()) {
            ProductImage entity = productImageMapper.toEntity(dto, product);
            product.getProductImages().add(entity);
        }

        // 저장
        Long id = productRepository.save(product).getProductId();
        log.info("Product created: id={} sellerId={} name={}", id, sellerId, req.name());
        return id;
    }

    /** 상품 이미지 최종 확정 */
    @Override
    public void finalizeImages(Long id, Long userId) {}

    // 디버깅 로그
    private void validateImages(ProductCreateRequest req) {
        if (req.images() == null || req.images().isEmpty()) {
            log.warn("images is null or empty");
            throw new CustomException(ErrorCode.INVALID_IMAGE_KEY);
        }

        log.debug("validateImages start size={} imgs={}", req.size(), req.images().size());

        long thumbs = req
                .images()
                .stream()
                .filter(ProductCreateRequest.ImageDto::isThumbnail)
                .count();

        if (thumbs > 1) {
            log.warn("thumbnail duplicated: thumbs={}", thumbs);
            throw new CustomException(ErrorCode.THUMBNAIL_DUPLICATED);
        }

        Set<Integer> orders = new HashSet<>();

        for (var img : req.images()) {
            if (!orders.add(img.sortOrder())) {
                log.warn("sortOrder duplicated: {}", img.sortOrder());
                throw new CustomException(ErrorCode.SORT_ORDER_DUPLICATED);
            }
            if (img.filePath() == null || !img.filePath().startsWith(TMP_PREFIX)) {
                log.warn("only tmp keys allowed: {}", img.filePath());
                throw new CustomException(ErrorCode.INVALID_IMAGE_KEY);
            }

            // S3에 객체가 존재하는지 확인 (PUT 실패/위조 키 차단)
            s3ObjectVerifier.requireExists(img.filePath());
        }
    }

    /** 상품의 판매자를 다른 사용자로 변경 */
    @Override
    @Transactional
    public void changeSeller(Long productId, Long newSellerId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        User newSeller = userRepository
                .findById(newSellerId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        try {
            product.changeSeller(newSeller); // 공개 도메인 메서드
        } catch (IllegalStateException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
        }
        // 영속 상태라 별도 save() 불필요
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(String sortBy, Integer limit) {
        try {
            // 1단계: 기본 상품 정보 조회 (페이징 적용)
            List<Product> products;

            if (limit != null && limit > 0) {
                Pageable pageable = PageRequest.of(0, limit);
                if (SORT_BY_OPTION.equals(sortBy)) {
                    products = productRepository.findAllOrderByBidCount(pageable);
                } else {
                    products = productRepository.findAllOrderByCreatedAtDesc(pageable);
                }
            } else {
                // limit이 없으면 기본 100개로 제한
                Pageable pageable = PageRequest.of(0, 100);
                if (SORT_BY_OPTION.equals(sortBy)) {
                    products = productRepository.findAllOrderByBidCount(pageable);
                } else {
                    products = productRepository.findAllOrderByCreatedAtDesc(pageable);
                }
            }

            // 2단계: N+1 문제 해결을 위한 연관 엔티티 페치 조인
            if (!products.isEmpty()) {
                List<Long> productIds = products
                        .stream()
                        .map(Product::getProductId)
                        .toList();

                // 이미지 정보 페치 조인
                products = productRepository.findByProductIdsWithImages(productIds);
            }

            // 3단계: DTO 변환
            return products
                    .stream()
                    .map(ProductResponse::fromEntity)
                    .toList();

        } catch (Exception e) {
            log.error("Error in getProducts: ", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        try {
            // 1단계: 검색 조건으로 상품 조회
            List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword);

            // 2단계: N+1 문제 해결을 위한 연관 엔티티 페치 조인
            if (!products.isEmpty()) {
                List<Long> productIds = products
                        .stream()
                        .map(Product::getProductId)
                        .toList();

                // 이미지 정보 페치 조인
                products = productRepository.findByProductIdsWithImages(productIds);
            }

            // 3단계: DTO 변환
            return products
                    .stream()
                    .map(ProductResponse::fromEntity)
                    .toList();
        } catch (Exception e) {
            log.error("Error in searchProducts: ", e);
            throw e;
        }
    }
}
