package com.sesac.solbid.controller.auction;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.auction.request.AuctionCreateRequest;
import com.sesac.solbid.dto.auction.response.AuctionCreateResponse;
import com.sesac.solbid.service.auction.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    /**
     * 경매 생성
     * POST /api/auctions
     *
     * @header JWT
     * @requestBody       AuctionCreateRequest (검증 대상)
     *                    - productId: 경매할 상품 ID
     *                    - startPrice: 시작가
     *                    - endAt: 경매 종료 일시(ISO-8601)
     *
     * @return            AuctionCreateResponse
     *                    - id: 생성된 경매 ID
     *                    또한 Location 헤더에 "/api/auctions/{id}" 반환
     *
     * @status 201 Created  성공적으로 생성, Location 헤더 포함
     * @status 404 Not Found     productId에 해당하는 상품 없음
     * @status 409 Conflict      경매 생성 규칙 충돌(예: 이미 진행 중인 경매)
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuctionCreateResponse> create(
            @AuthenticationPrincipal User authUser,
            @Valid @RequestBody AuctionCreateRequest req
    ) {
        Long userId = authUser.getUserId();

        log.info("POST /api/auctions by userId={} productId={} startPrice={} endAt={}",
                userId, req.productId(), req.startPrice(), req.endAt());

        Long id = auctionService.create(userId, req);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity
                .created(location) // 201 + Location: /api/auctions/{id}
                .body(new AuctionCreateResponse(id));
    }
}
