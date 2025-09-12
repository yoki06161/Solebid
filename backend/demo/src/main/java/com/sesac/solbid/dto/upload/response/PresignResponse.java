package com.sesac.solbid.dto.upload.response;

public record PresignResponse(
        String key,
        String putUrl,
        String publicUrl
) {}