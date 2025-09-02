package com.sesac.solbid.dto.payment.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortOneApproveResponse {

    private ApproveData response;

    @Getter
    @Setter
    public static class ApproveData {
        private String impUid;

        @JsonProperty("merchant_uid")
        private String orderId;  // = merchant_uid portone 에서 사용하는 고유 아이디

        private int amount;

        private String status;
    }
}
