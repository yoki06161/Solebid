package com.sesac.solbid.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class S3PresignServiceImpl implements S3Service {

    private final S3Presigner presigner;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.presign.expire-minutes:15}")
    private long defaultExpireMinutes;

    @Override
    public String presignPutUrl(String key, String contentType) {
        return presignPutUrl(key, contentType, defaultExpireMinutes);
    }

    @Override
    public String presignPutUrl(String key, String contentType, long expireMinutes) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(contentType, "contentType must not be null");

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        // 요청: PutObjectPresignRequest
        PutObjectPresignRequest req = PutObjectPresignRequest.builder()
                .putObjectRequest(put)
                .signatureDuration(Duration.ofMinutes(expireMinutes))
                .build();

        // 응답: PresignedPutObjectRequest
        PresignedPutObjectRequest presigned = presigner.presignPutObject(req);
        return presigned.url().toString();
    }

    @Override
    public String presignGetUrl(String key) {
        Objects.requireNonNull(key, "key must not be null");

        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest req = GetObjectPresignRequest.builder()
                .getObjectRequest(get)
                .signatureDuration(Duration.ofMinutes(defaultExpireMinutes))
                .build();

        PresignedGetObjectRequest presigned = presigner.presignGetObject(req);

        return presigned.url().toString();
    }
}
