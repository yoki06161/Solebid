package com.sesac.solbid.controller;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.payment.request.PaymentRecordSearchRequest;
import com.sesac.solbid.dto.payment.response.PageResponse;
import com.sesac.solbid.dto.payment.response.PaymentRecordItemResponse;
import com.sesac.solbid.service.payment.PaymentRecordQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/records")
@RequiredArgsConstructor
@Slf4j
public class PaymentRecordQueryController {

    private final PaymentRecordQueryService service;

    /**
     * 유저 아이디 기반 결제 내역 조회
     * GET /api/payments/records?userId=1
     * GET /api/payments/records?userId=1&status=SUCCESS&from=2025-08-01&to=2025-09-01
     * 페이징/정렬:
     *   ?page=0&size=20&sort=requestedAt,desc
     */
    @GetMapping
    public ResponseEntity<PageResponse<PaymentRecordItemResponse>> list(
            @Valid @ModelAttribute PaymentRecordSearchRequest req,
            @PageableDefault(size = 20, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User authUser
    ) {
        req.setUserId(authUser.getUserId());
        return ResponseEntity.ok(service.getRecords(req, pageable));
    }
}
