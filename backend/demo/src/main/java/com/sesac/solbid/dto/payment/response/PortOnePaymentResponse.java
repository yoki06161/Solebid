package com.sesac.solbid.dto.payment.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortOnePaymentResponse {

    private PaymentData response;

    @Getter
    @Setter
    public static class PaymentData {
        @JsonProperty("imp_uid")
        private String impUid;

        @JsonProperty("merchant_uid")
        private String orderId;

        private String status;
        private int amount;

        @JsonProperty("pay_method")
        private String payMethod;

        @JsonProperty("paid_at")
        private Long paidAt;
    }
}
