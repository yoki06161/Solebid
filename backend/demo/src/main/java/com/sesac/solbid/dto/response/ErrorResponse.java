package com.sesac.solbid.dto.response;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String code,
        String message,
        String timestamp,
        String traceId
) {}