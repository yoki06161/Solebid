package com.sesac.solbid.controller.auction;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.service.auction.AuctionBidService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auctions")
public class AuctionBidController {

    private final AuctionBidService bidService;

    public record BidRequest(
            @NotNull BigDecimal amount,
            @NotBlank String idempotencyKey
    ) {}


    /**
     * 경매 입찰
     * POST /api/auctions/{auctionId}/bids
     *
     * @header JWT
     * @pathVariable        auctionId (입찰 대상 경매 ID)
     * @requestBody         BidRequest (검증 대상)
     *                      - amount: 입찰 금액
     *                      - idempotencyKey: 멱등성 보장용
     * @return              { "success": true }
     */
    @PostMapping("/{auctionId}/bids")
    public Map<String, Object> bid(
            @PathVariable Long auctionId,
            @Valid @RequestBody BidRequest req,
            @AuthenticationPrincipal User authUser
    ) {
        Long userId = authUser.getUserId();
        bidService.placeBidWithRetry(auctionId, userId, req.amount(), req.idempotencyKey());
        return Map.of("success", true);
    }
}
