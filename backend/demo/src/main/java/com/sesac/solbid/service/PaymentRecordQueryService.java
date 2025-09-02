package com.sesac.solbid.service;

import com.sesac.solbid.dto.request.PaymentRecordSearchRequest;
import com.sesac.solbid.dto.response.PageResponse;
import com.sesac.solbid.dto.response.PaymentRecordItemResponse;
import org.springframework.data.domain.Pageable;

public interface PaymentRecordQueryService {
    PageResponse<PaymentRecordItemResponse> getRecords(PaymentRecordSearchRequest req, Pageable pageable);
}
