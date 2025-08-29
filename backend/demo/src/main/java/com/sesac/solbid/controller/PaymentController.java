package com.sesac.solbid.controller;

import com.sesac.solbid.dto.request.PaymentPrepareRequest;
import com.sesac.solbid.dto.response.PaymentPrepareResponse;
import com.sesac.solbid.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /*
    * 포트원 결제 요청 준비 */
    @PostMapping("/charge/prepare")
    public ResponseEntity<PaymentPrepareResponse> preparePayment(@RequestBody PaymentPrepareRequest request) {
        return ResponseEntity.ok(paymentService.preparePayment(request));
        /*
        * 서버에서 고유 orderId 생성
        * redirectUrl 반환*/
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "ts", System.currentTimeMillis());
    }




}
