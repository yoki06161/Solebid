package com.sesac.solbid.service.auction;

import com.sesac.solbid.domain.AuctionEvent;
import com.sesac.solbid.domain.ProductImage;
import com.sesac.solbid.dto.auction.response.AuctionEventCardResponse;
import com.sesac.solbid.repository.auction.AuctionEventCardRepository;
import com.sesac.solbid.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionEventCardService {

    private final AuctionEventCardRepository repository;
    private final S3Service s3Service;

    private static final Comparator<ProductImage> IMAGE_PRIORITY = Comparator
            .comparing(ProductImage::isThumbnail, Comparator.reverseOrder())
            .thenComparing(ProductImage::getSortOrder, Comparator.nullsLast(Integer::compareTo));

    public List<AuctionEventCardResponse> fetchActiveCards(int limit) {
        return repository.findActiveWithProduct(limit)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuctionEventCardResponse toResponse(AuctionEvent event) {
        var product = event.getProduct();

        String imageUrl = product.getProductImages()
                .stream()
                .sorted(IMAGE_PRIORITY)
                .map(ProductImage::getFilePath)
                .findFirst()
                .map(s3Service::presignGetUrl)
                .orElse(null);

        BigDecimal currentBid = event.getHighestBidAmount();
        if (currentBid == null) {
            currentBid = event.getStartPrice();
        }

        return new AuctionEventCardResponse(
                event.getAuctionEventId(),
                product.getProductId(),
                product.getProductBrand().name(),
                product.getName(),
                product.getProductCategory().name(),
                imageUrl,
                currentBid,
                event.getEndAt(),
                event.getViewCount()
        );
    }
}
