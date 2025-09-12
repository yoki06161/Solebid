package com.sesac.solbid.service;

import java.util.Map;

public interface UploadService {
    Map<String, String> presign(String fileName, String contentType);
}
