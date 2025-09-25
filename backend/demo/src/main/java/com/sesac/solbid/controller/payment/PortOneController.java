package com.sesac.solbid.controller.payment;

import com.sesac.solbid.service.payment.PaymentService;
import com.sesac.solbid.service.payment.PortOneService;
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

    /**
     * 포트원 액세스 토큰 발급 테스트
     * GET /api/portone/token
     *
     * @return 포트원 API 액세스 토큰 문자열
     */
    @GetMapping("/token")
    public ResponseEntity<String> getToken() {
        String token = portOneService.getAccessToken();
        return ResponseEntity.ok(token);
    }

    /*
    결제 승인 요청 테스트
    @GetMapping("/approve")
    public ResponseEntity<String> approve1(@RequestParam String impUid) {
        String token = portOneService.getAccessToken();
        String status = portOneService.approvePayment(impUid, token);
        return ResponseEntity.ok("결제 상태: " + status);
    }


    // 결제 승인 요청 테스트 2
    // PortOneController.java
    @GetMapping("/approve")
    public ResponseEntity<String> approve(@RequestParam String impUid) {
        String result = paymentService.handlePaymentSuccess(impUid);
        return ResponseEntity.ok(result);
    }
    */


    /**
     * imp_uid 기반 결제 승인 및 처리
     * GET /api/portone/approve?impUid={imp_uid}
     *
     * @param impUid PortOne 결제 고유 ID
     * @return 처리 결과 문자열
     **/
    @GetMapping("/approve")
    public ResponseEntity<String> approve(@RequestParam String impUid) {
        String token = portOneService.getAccessToken();
        String result = paymentService.handlePaymentSuccess(impUid, token);
        return ResponseEntity.ok(result);
    }


}

