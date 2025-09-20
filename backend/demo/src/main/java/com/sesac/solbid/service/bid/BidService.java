package com.sesac.solbid.service.bid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sesac.solbid.domain.Bid;
import com.sesac.solbid.dto.bid.response.BidResponse;
import com.sesac.solbid.repository.bid.BidRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;

    @Transactional(readOnly = true)
    public List<BidResponse> getBidsWinning(Long userId) {
        log.info("사용자 낙찰 내역 조회 시작: userId={}", userId);

        List<Bid> bids = bidRepository.findByBidderUserIdAndIsWinningTrueOrderByBidTimeDesc(userId);
        log.info("데이터베이스에서 조회된 낙찰 내역: {} 건", bids.size());

        List<BidResponse> responses = bids.stream()
                .map(this::convertToWinningBidResponse)
                .collect(Collectors.toList());
        log.info("BidResponse 변환 완료: {} 건", responses.size());

        return responses;
    }

    private BidResponse convertToWinningBidResponse(Bid bid) {
        var product = bid.getAuctionEvent().getProduct();

        String imageUrl = null;

        try {
            if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
                imageUrl = product.getProductImages().get(0).getFilePath();
            }
        } catch (Exception e) {
            log.warn("상품 이미지 로드 실패: productId={}", product.getProductId(), e);
        }

        return new BidResponse(
                bid.getBidId(),
                product.getProductId(),
                product.getName(),
                imageUrl,
                bid.getBidAmount(),
                bid.getBidTime(),
                product.getProductBrand() != null ? product.getProductBrand().name() : "UNKNOWN",
                product.getProductCategory() != null ? product.getProductCategory().name() : "UNKNOWN",
                product.getSize());
    }
}
