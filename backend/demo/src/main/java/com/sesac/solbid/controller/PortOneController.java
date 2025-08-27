package com.sesac.solbid.controller;

import com.sesac.solbid.service.PaymentService;
import com.sesac.solbid.service.PortOneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portone")
public class PortOneController {

    private final PortOneService portOneService;
    private final PaymentService paymentService;

    // 액세스 토큰 테스트용 API
    @GetMapping("/token")
    public ResponseEntity<String> getToken() {
        String token = portOneService.getAccessToken();
        return ResponseEntity.ok(token);
    }

    /*
    // 결제 승인 요청 테스트
    @GetMapping("/approve")
    public ResponseEntity<String> approve1(@RequestParam String impUid) {
        String token = portOneService.getAccessToken();
        String status = portOneService.approvePayment(impUid, token);
        return ResponseEntity.ok("결제 상태: " + status);
    }
*/
    /*
    // 결제 승인 요청 테스트 2
    // PortOneController.java
    @GetMapping("/approve")
    public ResponseEntity<String> approve(@RequestParam String impUid) {
        String result = paymentService.handlePaymentSuccess(impUid);
        return ResponseEntity.ok(result);
    }
*/


    // imp_uid 기반 결제 승인 및 처리
    @GetMapping("/approve")
    public ResponseEntity<String> approve(@RequestParam String impUid) {
        String token = portOneService.getAccessToken();
        String result = paymentService.handlePaymentSuccess(impUid, token);
        return ResponseEntity.ok(result);
    }


}

