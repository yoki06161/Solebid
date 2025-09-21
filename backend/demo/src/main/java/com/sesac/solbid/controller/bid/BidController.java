package com.sesac.solbid.controller.bid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.ApiResponse;
import com.sesac.solbid.dto.bid.response.BidSellingResponse;
import com.sesac.solbid.dto.bid.response.BidWinningResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.ProductRepository;
import com.sesac.solbid.repository.bid.BidRepository;
import com.sesac.solbid.service.bid.BidService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bids")
public class BidController {

    private final BidService bidService;
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;

    // 낙찰 내역 조회
    @GetMapping("/winning")
    public ResponseEntity<ApiResponse<List<BidWinningResponse>>> getBidsWinning(@AuthenticationPrincipal User user) {
        log.info("낙찰 내역 조회 요청 시작");

        if (user == null) {
            log.warn("인증되지 않은 사용자의 낙찰 내역 조회 시도");
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        try {
            log.info("사용자 낙찰 내역 조회: userId={}", user.getUserId());

            List<BidWinningResponse> winningBids = bidService.getBidsWinning(user.getUserId());
            log.info("낙찰 내역 조회 완료: {} 건", winningBids.size());

            return ResponseEntity.ok(ApiResponse.success(winningBids));
        } catch (Exception e) {
            log.error("낙찰 내역 조회 중 예외 발생", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    // 판매 내역 조회
    @GetMapping("/selling")
    public ResponseEntity<ApiResponse<List<BidSellingResponse>>> getBidsSelling(
            @AuthenticationPrincipal User authUser) {
        log.info("판매 내역 조회 요청 시작");

        if (authUser == null) {
            log.warn("인증되지 않은 사용자의 판매 내역 조회 시도");
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        try {
            log.info("사용자 판매 내역 조회: userId={}", authUser.getUserId());

            List<BidSellingResponse> soldProducts = bidService.getBidSelling(authUser.getUserId());
            log.info("판매 내역 조회 완료: {} 건", soldProducts.size());

            return ResponseEntity.ok(ApiResponse.success(soldProducts));
        } catch (Exception e) {
            log.error("판매 내역 조회 중 예외 발생", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }
}
