package com.sesac.solbid.service.payment;

import com.sesac.solbid.dto.payment.request.PaymentRecordSearchRequest;
import com.sesac.solbid.dto.payment.response.PageResponse;
import com.sesac.solbid.dto.payment.response.PaymentRecordItemResponse;
import org.springframework.data.domain.Pageable;

public interface PaymentRecordQueryService {
    /**사용자 결제 내역 조회*/
    PageResponse<PaymentRecordItemResponse> getRecords(PaymentRecordSearchRequest req, Pageable pageable);
}
