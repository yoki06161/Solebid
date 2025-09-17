package com.sesac.solbid.service.s3;

public interface S3Service {

    /**Presigned S3 업로드 Key 발급*/
    String presignPutUrl(String key, String contentType);
    default String presignPutUrl(String key, String contentType, long expireMinutes) {
        return presignPutUrl(key, contentType);
    }
    
    String presignGetUrl(String key);
}
