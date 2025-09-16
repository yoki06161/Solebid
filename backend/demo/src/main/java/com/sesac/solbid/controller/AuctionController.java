package com.sesac.solbid.controller;

import com.sesac.solbid.dto.auction.request.AuctionCreateRequest;
import com.sesac.solbid.dto.auction.response.AuctionCreateResponse;
import com.sesac.solbid.service.auction.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
     * @header X-User-Id  인증된 사용자 ID (Long) -> 후애 JWT 변경
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
     * @status 400 Bad Request  요청 본문 검증 실패(@Valid)
     * @status 401 Unauthorized  사용자 인증/헤더 누락 등
     * @status 404 Not Found     productId에 해당하는 상품 없음
     * @status 409 Conflict      경매 생성 규칙 충돌(예: 이미 진행 중인 경매)
     * @status 500 Internal Server Error  서버 오류
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuctionCreateResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AuctionCreateRequest req
    ) {
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
