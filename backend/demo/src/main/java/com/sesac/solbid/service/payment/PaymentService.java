package com.sesac.solbid.service.payment;

import com.sesac.solbid.dto.payment.request.PaymentPrepareRequest;
import com.sesac.solbid.dto.payment.response.PaymentPrepareResponse;


public interface PaymentService {

    /**결제 준비*/
    PaymentPrepareResponse preparePayment(PaymentPrepareRequest request);
    /**결제 승인*/
    String handlePaymentSuccess(String impUid, String token);




}
