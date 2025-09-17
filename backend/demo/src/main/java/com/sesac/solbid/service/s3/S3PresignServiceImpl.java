package com.sesac.solbid.service.s3;

import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3PresignServiceImpl implements S3Service {

    private final S3Presigner presigner;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static:}")
    private String regionStr;

    @Value("${app.s3.presign.expire-minutes:15}")
    private long defaultExpireMinutes;

    /**Presigned S3 업로드 Key 발급*/
    @Override
    public String presignPutUrl(String key, String contentType) {
        try {
            if (bucket == null || bucket.isBlank()) {
                log.error("S3 bucket is not configured. (app.s3.bucket is null/blank)");
                throw new CustomException(ErrorCode.S3_IO_ERROR);
            }
            if (contentType == null || contentType.isBlank()) {
                contentType = "image/jpeg";
            }

            PutObjectRequest put = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            var req = PutObjectPresignRequest.builder()
                    .putObjectRequest(put)
                    .signatureDuration(Duration.ofMinutes(15))
                    .build();

            var presigned = presigner.presignPutObject(req);
            log.info("Presign OK: bucket={} region={} key={}", bucket, regionStr, key);
            return presigned.url().toString();

        } catch (Exception e) {
            log.error("Presign failed. bucket={} region={} key={} ct={} msg={}",
                    bucket, regionStr, key, contentType, e.getMessage(), e);
            throw new CustomException(ErrorCode.S3_IO_ERROR);
        }


    }

    @Override
    public String presignGetUrl (String key){
        Objects.requireNonNull(key, "key must not be null");

        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest req = GetObjectPresignRequest.builder()
                .getObjectRequest(get)
                .signatureDuration(Duration.ofMinutes(
                        defaultExpireMinutes))
                .build();

        PresignedGetObjectRequest presigned = presigner.presignGetObject(req);

        return presigned.url().toString();
    }

}
