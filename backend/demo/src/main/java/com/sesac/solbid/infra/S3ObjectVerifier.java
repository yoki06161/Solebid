package com.sesac.solbid.infra;

import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ObjectVerifier {

    private final S3Client s3;

    @Value("${app.s3.bucket}")
    private String bucket;

    /** S3에 객체가 실제로 존재하는지 HEAD로 확인.
     * 없으면 INVALID_IMAGE_KEY */
    public void requireExists(String key) {
        if (key == null || !key.startsWith("products/")) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_KEY);
        }
        try {
            s3.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (AwsServiceException e) { // 4xx/5xx
            String code = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "N/A";
            Integer status = e.statusCode();
            String reqId = e.requestId();
            log.warn("S3 headObject failed. bucket={}, key={}, status={}, code={}, reqId={}, msg={}",
                    bucket, key, status, code, reqId, e.getMessage(), e);
            throw new CustomException(ErrorCode.INVALID_IMAGE_KEY);
        } catch (SdkClientException e) {
            log.error("S3 headObject client error. bucket={}, key={}, msg={}", bucket, key, e.getMessage(), e);
            throw new CustomException(ErrorCode.S3_IO_ERROR);
        }
    }
}