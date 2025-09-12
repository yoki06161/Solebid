package com.sesac.solbid.service;

import com.sesac.solbid.util.UploadPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final S3Service s3Service;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.region:ap-northeast-2}")
    private String region;

    @Value("${app.cdn.base-url:}")
    private String cdnBase;

    @Override
    public Map<String, String> presign(String fileName, String contentType) {
        UploadPolicy.requireAllowed(contentType);
        String ext = UploadPolicy.extOf(contentType);

        YearMonth ym = YearMonth.now(ZoneId.of("Asia/Seoul"));
        String key = "products/tmp/%d%02d/%s.%s".formatted(
                ym.getYear(), ym.getMonthValue(), UUID.randomUUID(), ext
        );

        String putUrl = s3Service.presignPutUrl(key, contentType);
        String publicUrl = buildPublicUrl(key);

        return Map.of(
                "key", key,
                "putUrl", putUrl,
                "publicUrl", publicUrl
        );
    }

    private String buildPublicUrl(String key) {
        if (cdnBase != null && !cdnBase.isBlank()) {
            return cdnBase.endsWith("/") ? cdnBase + key : cdnBase + "/" + key;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
