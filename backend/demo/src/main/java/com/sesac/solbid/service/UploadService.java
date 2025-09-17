package com.sesac.solbid.service;

import java.util.List;
import java.util.Map;

public interface UploadService {
    /**업로드용 Presigned URL을 발급*/
    Map<String, String> presign(String fileName, String contentType);
    /**S3 이미지 key 목록 다운로드 URL 생성*/
    Map<String, String> getDownloadUrls(List<String> imageKeys);
}
