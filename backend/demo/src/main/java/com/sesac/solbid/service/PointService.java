package com.sesac.solbid.service;

import com.sesac.solbid.dto.payment.response.PointSummaryResponse;

public interface PointService {
    PointSummaryResponse getCurrentPoint(Long userId);
}