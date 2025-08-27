package com.sesac.solbid.dto.response;


import lombok.Data;

@Data
public class PaymentPrepareResponse { //포트원 결제 response
    private String orderId;
    private String redirectUrl;

    public PaymentPrepareResponse(String orderId, String redirectUrl) {
        this.orderId = orderId;
        this.redirectUrl = redirectUrl;
    }

}
