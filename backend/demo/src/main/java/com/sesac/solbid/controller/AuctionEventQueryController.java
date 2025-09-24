package com.sesac.solbid.controller;

import com.sesac.solbid.dto.auction.response.AuctionEventCardResponse;
import com.sesac.solbid.service.auction.AuctionEventCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auction-events")
public class AuctionEventQueryController {

    private final AuctionEventCardService cardService;

    /**
     * 활성화된 경매 이벤트 카드 조회
     *
     * @param limit 조회할 경매 카드의 최대 개수 (1~100, 기본값 30)
     * @return 활성화된 경매 이벤트를 나타내는 {@link AuctionEventCardResponse} 리스트
     * */
    @GetMapping("/cards")
    public List<AuctionEventCardResponse> list(
            @RequestParam(name = "limit", defaultValue = "30") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return cardService.fetchActiveCards(safeLimit);
    }
}
