package com.sesac.solbid.service;

import java.util.List;
import java.util.Map;

public interface UploadService {
    Map<String, String> presign(String fileName, String contentType);
    Map<String, String> getDownloadUrls(List<String> imageKeys);
}
