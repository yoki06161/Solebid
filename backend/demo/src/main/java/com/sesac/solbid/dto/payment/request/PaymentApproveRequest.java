package com.sesac.solbid.dto.payment.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentApproveRequest {
    private String impUid; // 포트원 결제 고유 ID
}
