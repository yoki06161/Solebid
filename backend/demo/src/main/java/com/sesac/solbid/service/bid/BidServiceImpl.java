package com.sesac.solbid.service.bid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sesac.solbid.domain.Bid;
import com.sesac.solbid.domain.Product;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.ProductStatus;
import com.sesac.solbid.dto.bid.response.BidSellingResponse;
import com.sesac.solbid.dto.bid.response.BidWinningResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.ProductRepository;
import com.sesac.solbid.repository.UserRepository;
import com.sesac.solbid.repository.bid.BidRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;
    private final BidConverter bidConverter;

    /** 사용자의 낙찰 상품 목록 조회 */
    @Override
    @Transactional(readOnly = true)
    public List<BidWinningResponse> getBidsWinning(Long userId) {
        log.info("사용자 낙찰 내역 조회 시작: userId={}", userId);

        List<Bid> bids = bidRepository.findByBidderUserIdAndIsWinningTrueOrderByBidTimeDesc(userId);
        log.info("데이터베이스에서 조회된 낙찰 내역: {} 건", bids.size());

        List<BidWinningResponse> responses = bids
                .stream()
                .map(bidConverter::convertToBidWinningResponse)
                .collect(Collectors.toList());
        log.info("BidResponse 변환 완료: {} 건", responses.size());

        return responses;
    }

    /** 판매자의 판매 완료 상품 목록 조회 */
    @Override
    @Transactional(readOnly = true)
    public List<BidSellingResponse> getBidSelling(Long sellerId) {
        log.info("판매 내역 조회 시작: sellerId={}", sellerId);

        try {
            // 판매자 조회
            User seller = userRepository
                    .findById(sellerId)
                    .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
            log.info("판매자 조회 완료: sellerName={}", seller.getName());

            // 판매 완료된 상품 목록 조회
            List<Product> soldProducts = productRepository
                    .findBySellerAndProductStatusOrderByUpdatedAtDesc(seller, ProductStatus.SOLD_OUT);
            log.info("판매 완료 상품 조회 완료: {} 건", soldProducts.size());

            // 각 상품의 최고 입찰가를 조회하여 BidSellingResponse 생성
            return soldProducts
                    .stream()
                    .map(product -> {
                        log.info("처리 중인 상품: productId={}, name={}", product.getProductId(), product.getName());
                        
                        // BidConverter의 convertToBidSellingResponse 메서드를 사용
                        // 이 메서드는 내부적으로 최고 입찰가를 조회하여 처리함
                        return bidConverter.convertToBidSellingResponse(product);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("판매 내역 조회 중 오류 발생: sellerId={}", sellerId, e);
            throw e;
        }
    }

    /** 특정 상품의 최고 입찰자를 낙찰자로 설정 */
    @Override
    @Transactional
    public void markWinningBidForProduct(Long productId) {
        log.info("상품 낙찰 처리 시작: productId={}", productId);
        
        try {
            // 해당 상품의 모든 입찰을 낙찰 해제
            List<Bid> allBids = bidRepository.findTopBidsByProductId(productId);
            allBids.forEach(Bid::unmarkAsWinning);
            
            if (!allBids.isEmpty()) {
                // 최고 입찰가를 낙찰로 설정
                Bid winningBid = allBids.get(0); // 이미 bidAmount DESC로 정렬됨
                winningBid.markAsWinning();
                
                log.info("낙찰 처리 완료: productId={}, winningBidId={}, amount={}", 
                        productId, winningBid.getBidId(), winningBid.getBidAmount());
            } else {
                log.warn("입찰이 없는 상품: productId={}", productId);
            }
        } catch (Exception e) {
            log.error("낙찰 처리 중 오류 발생: productId={}", productId, e);
            throw e;
        }
    }
}
