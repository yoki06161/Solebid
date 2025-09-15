package com.sesac.solbid.dto.upload.request;

import java.util.List;

public record DownloadUrlRequest(
        List<String> imageKeys
) {}
