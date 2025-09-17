package com.sesac.solbid.service.s3;

import org.springframework.web.multipart.MultipartFile;

public interface S3StorageService {
    /** 서버에서 직접 업로드하고 '키'를 반환 */
    String uploadAndReturnKey(MultipartFile file);

    /** (호환용) 서버 업로드 후 Public URL 반환 */
    String uploadAndReturnPublicUrl(MultipartFile file);

    /** 키로 다운로드하여 MultipartFile 형태 반환 */
    org.springframework.web.multipart.MultipartFile download(String key);

    /** 키로 퍼블릭 URL 구성(cdnBase 있으면 CDN, 없으면 S3 URL) */
    String buildPublicUrl(String key);
}
