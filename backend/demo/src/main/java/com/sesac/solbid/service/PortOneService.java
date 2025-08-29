package com.sesac.solbid.service;

import com.sesac.solbid.dto.response.PortOnePaymentResponse;
import org.springframework.stereotype.Service;


public interface PortOneService {

    // 결제 승인 토큰
    String getAccessToken();
    // 결제 승인 준비
    PortOnePaymentResponse.PaymentData approvePayment(String impUid, String accessToken);


}
