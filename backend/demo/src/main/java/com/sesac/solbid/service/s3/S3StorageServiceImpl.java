package com.sesac.solbid.service.s3;

import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.upload.KeyFactory;
import com.sesac.solbid.util.S3MultipartFile;
import com.sesac.solbid.util.UploadPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageServiceImpl implements S3StorageService {

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.region:ap-northeast-2}")
    private String region;

    @Value("${app.cdn.base-url:}")
    private String cdnBase;

    @Value("${app.s3.server-upload.public-read:false}")
    private boolean publicRead;

    private final S3Client s3Client;
    private final KeyFactory keyFactory;

    /**MultipartFile을 S3에 업로드 후 생성된 Key 반환 (임시 경로에 저장됨)*/
    @Override
    public String uploadAndReturnKey(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            UploadPolicy.requireAllowed(contentType);
            String ext = UploadPolicy.extOf(contentType);

            String key = keyFactory.tmpKey(ext); // tmp로 올리고 나중에 finalize 이동

            PutObjectRequest.Builder pob = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType);
            if (publicRead) {
                pob.acl(ObjectCannedACL.PUBLIC_READ); // 필요시에만 public
            }

            s3Client.putObject(pob.build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("S3 upload ok: key={}", key);
            return key;
        } catch (IOException e) {
            log.error("S3 upload IO error", e);
            throw new CustomException(ErrorCode.S3_IO_ERROR);
        } catch (S3Exception e) {
            log.error("S3 upload failed: {}", e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage(), e);
            throw new CustomException(ErrorCode.S3_IO_ERROR);
        }
    }

    /**MultipartFile을 S3에 업로드 후 Public URL 반환.*/
    @Override
    public String uploadAndReturnPublicUrl(MultipartFile file) {
        String key = uploadAndReturnKey(file);
        return buildPublicUrl(key);
    }

    /**S3에서 key 기반으로 객체 다운로드 후 MultipartFile로 반환.*/
    @Override
    public MultipartFile download(String key) {
        try {
            var getReq = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(getReq);
            return new S3MultipartFile(bytes.asByteArray(), key, bytes.response().contentType());
        } catch (S3Exception e) {
            log.error("S3 download failed: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.S3_IO_ERROR);
        }
    }

    /**주어진 S3 key에 대한 Public URL 생성.
     * CDN이 설정되어 있으면 CDN base URL을, 없으면 기본 S3 URL을 반환.*/
    @Override
    public String buildPublicUrl(String key) {
        if (cdnBase != null && !cdnBase.isBlank()) {
            return cdnBase.endsWith("/") ? cdnBase + key : cdnBase + "/" + key;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
