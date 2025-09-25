// com/sesac/solbid/controller/AuctionAdminController.java
package com.sesac.solbid.controller.auction;

import com.sesac.solbid.service.auction.AuctionSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

        import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auctions")
public class AuctionAdminController {

    private final AuctionSettlementService settlement;

    /**
     * 특정 경매 즉시 정산 처리 테스트/운영용 수동 종료 트리거
     * POST /api/admin/auctions/{auctionId}/finalize
     *
     * @param auctionId 종료 및 정산을 강제로 진행할 경매의 ID
     * @return {@code {"ok": true}} 형태의 응답 맵 (성공 여부 표시)
     */
    @PostMapping("/{auctionId}/finalize")
    public Map<String, Object> finalizeNow(@PathVariable Long auctionId) {
        settlement.finalizeIfDueTx(auctionId, LocalDateTime.now());
        return Map.of("ok", true);
    }

}