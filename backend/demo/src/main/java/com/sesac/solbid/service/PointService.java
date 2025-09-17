package com.sesac.solbid.service;

import com.sesac.solbid.dto.payment.response.PointSummaryResponse;

public interface PointService {
    /**유저 포인트 단건 조회*/
    PointSummaryResponse getCurrentPoint(Long userId);
}