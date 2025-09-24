package com.sesac.solbid.controller;

import com.sesac.solbid.dto.payment.request.PaymentPrepareRequest;
import com.sesac.solbid.dto.payment.response.PaymentPrepareResponse;
import com.sesac.solbid.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 포트원 결제 요청 준비
     * POST /api/payments/charge/prepare
     *
     * @param request 결제 준비 요청 DTO
     * @return 결제 준비 응답 DTO(orderId, redirectUrl 포함)
     **/
    @PostMapping("/charge/prepare")
    public ResponseEntity<PaymentPrepareResponse> preparePayment(@RequestBody PaymentPrepareRequest request) {
        return ResponseEntity.ok(paymentService.preparePayment(request));
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "ts", System.currentTimeMillis());
    }

}
