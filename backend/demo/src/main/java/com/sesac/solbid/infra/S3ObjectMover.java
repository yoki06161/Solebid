package com.sesac.solbid.infra;

import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.upload.KeyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.MetadataDirective;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ObjectMover {

    private final S3Client s3;
    private final KeyFactory keyFactory;

    @Value("${app.s3.bucket}")
    private String bucket;

    private static final String KEY_PREFIX = "products/";
    private static final String TMP_PREFIX = KEY_PREFIX + "tmp/";

    /**
     * tmp 경로(products/tmp/...)면 products/{productId}/... 로 이동(copy+delete) 후 새 키를 반환.
     * tmp가 아니면 원래 키 그대로 반환.
     */
    public String moveTmpToFinal(String key, Long productId) {
        if (key == null || !key.startsWith(KEY_PREFIX)) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_KEY);
        }
        if (!key.startsWith(TMP_PREFIX)) {
            return key; // 이미 최종 경로
        }

        String newKey = keyFactory.finalKey(productId, key);

        try {
            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(key)
                    .destinationBucket(bucket)
                    .destinationKey(newKey)
                    .metadataDirective(MetadataDirective.COPY) // 메타 유지 명시
                    .build();
            s3.copyObject(copyReq);

            // 검증
            s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(newKey).build());

            // 삭제 (실패는 경고)
            try {
                s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            } catch (S3Exception delEx) {
                log.warn("S3 delete tmp failed (will be cleaned later). key={} msg={}", key, delEx.getMessage());
            }

            log.info("S3 moved: {} -> {}", key, newKey);
            return newKey;

        } catch (S3Exception e) {
            log.error("S3 move failed. from={} to={} (bucket={}) msg={}", key, newKey, bucket, e.getMessage(), e);
            throw new CustomException(ErrorCode.S3_IO_ERROR);
        }
    }
}

