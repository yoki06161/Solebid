package com.sesac.solbid.dto.upload.request;

public record PresignRequest(
        String fileName,
        String contentType
) {}
