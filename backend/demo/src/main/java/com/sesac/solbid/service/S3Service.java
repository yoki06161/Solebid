package com.sesac.solbid.service;

public interface S3Service {
    String presignPutUrl(String key, String contentType);
    default String presignPutUrl(String key, String contentType, long expireMinutes) {
        return presignPutUrl(key, contentType);
    }
}
