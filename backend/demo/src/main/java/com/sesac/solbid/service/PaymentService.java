package com.sesac.solbid.service;

import com.sesac.solbid.dto.request.PaymentPrepareRequest;
import com.sesac.solbid.dto.response.PaymentPrepareResponse;
import org.springframework.stereotype.Service;


public interface PaymentService {

    //결제 준비
    PaymentPrepareResponse preparePayment(PaymentPrepareRequest request);
    //결제 승인
    String handlePaymentSuccess(String impUid, String token);




}
