package com.sesac.solbid.service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sesac.solbid.util.UploadPolicy;

import lombok.RequiredArgsConstructor;

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

    /**업로드용 Presigned URL 발급*/
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
                "publicUrl", publicUrl);
    }

    @Override
    public Map<String, String> getDownloadUrls(List<String> imageKeys) {
        return imageKeys
                .stream()
                .distinct()
                .collect(Collectors.toMap(Function.identity(), s3Service::presignGetUrl));
    }

    /**key에 대한 public URL을 생성*/
    private String buildPublicUrl(String key) {
        if (cdnBase != null && !cdnBase.isBlank()) {
            return cdnBase.endsWith("/") ? cdnBase + key : cdnBase + "/" + key;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
