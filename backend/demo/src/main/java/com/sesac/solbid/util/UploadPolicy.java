package com.sesac.solbid.util;

import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;


import java.util.Set;

public class UploadPolicy {
    private UploadPolicy() {} // 유틸 클래스로만 사용

    // 허용할 MIME 타입
    public static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png"
    );

    /** 허용 타입인지 검사 (아니면 예외) */
    public static void requireAllowed(String contentType) {
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }
    }

    /** Content-Type → 확장자 매핑 */
    public static String extOf(String contentType) {
        if ("image/jpeg".equals(contentType)) return "jpg";
        if ("image/png".equals(contentType))  return "png";
        throw new CustomException(ErrorCode.UNSUPPORTED_CONTENT_TYPE);
    }

    /** 과거 예시 호환용: 파일명은 무시하고 Content-Type 기준으로 통일 */
    public static String normalizeExt(String contentType, String fileName) {
        return extOf(contentType);
    }

}
