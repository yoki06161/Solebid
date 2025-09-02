package com.sesac.solbid.service;

import com.sesac.solbid.dto.payment.request.PaymentRecordSearchRequest;
import com.sesac.solbid.dto.payment.response.PageResponse;
import com.sesac.solbid.dto.payment.response.PaymentRecordItemResponse;
import org.springframework.data.domain.Pageable;

public interface PaymentRecordQueryService {
    PageResponse<PaymentRecordItemResponse> getRecords(PaymentRecordSearchRequest req, Pageable pageable);
}
