package com.sesac.solbid.controller;

import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {

    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    /**
     * S3 키를 받아 이미지/파일을 스트리밍 반환
     * GET /api/files/products/tmp/202509/example.png
     *
     * @param request   {@link HttpServletRequest}.
     *                  <code>/api/files/</code> 이하 전체 경로를 추출 시 사용
     * @param authUser  인증된 사용자 객체({@code null}일 수 있음)
     *                  단순히 로그 기록용으로만 사용
     * @return          S3 객체를 스트리밍 형태의 {@link Resource}로 담은 {@link ResponseEntity}
     *                  HTTP 헤더와 콘텐츠 타입이 함께 설정되어 반환
     */
    @GetMapping("/api/files/**")
    public ResponseEntity<Resource> getFile(
            HttpServletRequest request,
            @AuthenticationPrincipal User authUser
    ) {
        // /api/files/ 이하 전체를 key로 사용
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String rawKey = path.replaceFirst("/api/files/", "");
        // 브라우저/프록시가 인코딩했을 경우 대비
        String key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);

        log.info("Serving S3 file: bucket={}, key={}, by={}",
                bucket, key, (authUser != null ? authUser.getUsername() : "anonymous"));

        try {
            GetObjectRequest get = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            // S3 스트림을 반환
            ResponseInputStream<?> s3Stream = s3Client.getObject(get);
            Resource resource = new InputStreamResource(s3Stream);

            MediaType mediaType = detectMediaType(key);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileNameOf(key) + "\"")
                    // 필요 시 캐시 정책 조정 예정
                    .cacheControl(CacheControl.noCache())
                    .body(resource);

        } catch (Exception e) {
            log.error("S3 file serving error: bucket={}, key={}, msg={}", bucket, key, e.getMessage(), e);
            throw new CustomException(ErrorCode.S3_IO_ERROR);
        }
    }

    private static String fileNameOf(String key) {
        int idx = key.lastIndexOf('/');
        return (idx >= 0 ? key.substring(idx + 1) : key);
    }

    /** 확장자 기반 Content-Type */
    private static MediaType detectMediaType(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png"))  return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        /* content-type 확장 고려
        if (lower.endsWith(".gif"))  return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (lower.endsWith(".svg"))  return MediaType.parseMediaType("image/svg+xml");
         */
        return MediaType.APPLICATION_OCTET_STREAM;
    }


}
