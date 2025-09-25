package com.sesac.solbid.controller.auction;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.auction.response.AuctionDetailResponse;
import com.sesac.solbid.service.auction.AuctionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
public class AuctionQueryController {

    private final AuctionQueryService auctionQueryService;

    /**
     * 경매 상세 조회
     * GET /api/auctions/{auctionId}
     *
     * @header JWT (선택적)
     * @pathVariable        auctionId (조회할 경매 ID)
     * @return              AuctionDetailResponse
     * */
    @GetMapping("/{auctionId}")
    public AuctionDetailResponse getDetail(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal User authUser
    ) {
        // 지금은 사용하지 않지만, 권한 체크나 viewCount 증가 시에 활용 가능
        return auctionQueryService.getAuctionDetail(auctionId);
    }
}