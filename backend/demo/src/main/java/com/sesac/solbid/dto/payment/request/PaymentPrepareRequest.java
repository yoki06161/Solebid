package com.sesac.solbid.dto.payment.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentPrepareRequest { //포트원 결제 request
    private int amount;
    private String paymentMethod;
    private String redirectUrl;
}
