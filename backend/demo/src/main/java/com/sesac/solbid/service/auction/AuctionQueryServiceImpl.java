package com.sesac.solbid.service.auction;

import com.sesac.solbid.domain.AuctionEvent;
import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.ProductImage;
import com.sesac.solbid.dto.auction.response.AuctionDetailResponse;
import com.sesac.solbid.repository.AuctionEventRepository;
import com.sesac.solbid.repository.ProductImageRepository;
import com.sesac.solbid.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionQueryServiceImpl implements AuctionQueryService {

    private final AuctionEventRepository auctionEventRepository;
    private final ProductImageRepository productImageRepository;
    private final S3Service s3Service; // Presigned URL

    @Override
    public AuctionDetailResponse getAuctionDetail(Long auctionId) {
        AuctionEvent a = auctionEventRepository.findDetail(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("AUCTION_NOT_FOUND"));

        Product p = a.getProduct();
        List<ProductImage> images = productImageRepository.findByProductOrderBySortOrderAsc(p);

        BigDecimal current = (a.getHighestBidAmount() == null) ? a.getStartPrice() : a.getHighestBidAmount();

        return new AuctionDetailResponse(
                a.getAuctionEventId(),
                a.getStatus(),
                a.getStartPrice(),
                a.getBuyoutPrice(),
                current,
                a.getTickSize(),
                a.getStartAt(),
                a.getEndAt(),
                a.getExtendSeconds(),
                a.getViewCount(),
                a.getIsBlind(),
                a.getVersion() == null ? 0L : a.getVersion(),
                new AuctionDetailResponse.ProductSummary(
                        p.getProductId(),
                        p.getName(),
                        p.getProductBrand().name(),
                        p.getProductCategory().name(),
                        p.getSize(),
                        p.getProductCondition().name(),
                        p.getModelCode(),
                        p.getColorway(),
                        p.getReleaseDate(),
                        images.stream()
                                .map(img -> new AuctionDetailResponse.ProductImage(
                                        s3Service.presignGetUrl(img.getFilePath()), //Presigned URL 변환
                                        img.isThumbnail(),
                                        img.getSortOrder()
                                ))
                                .toList()
                )
        );
    }
}
