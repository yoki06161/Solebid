package com.sesac.solbid.controller.upload;

import com.sesac.solbid.dto.upload.request.PresignRequest;
import com.sesac.solbid.dto.upload.response.PresignResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.upload.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sesac.solbid.dto.upload.request.DownloadUrlRequest;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;

    /**
     * 파일 업로드용 Presigned URL 발급
     * POST /api/uploads/presign
     *
     * @param req 파일 이름 및 Content-Type을 담은 요청 DTO
     * @return key, putUrl, publicUrl을 담은 {@link PresignResponse}
     *
     * URL 생성 후 저장
     * 서버 { key, putUrl, publicUrl } 응답
     * 업로드 URL만 발급
     * */
    @PostMapping("/presign")
    public PresignResponse presign(@RequestBody PresignRequest req){
        log.info("POST /api/uploads/presign fileName={}, contentType={}", req.fileName(), req.contentType());
        String ct = (req.contentType() == null || req.contentType().isBlank()) ? "image/jpeg" : req.contentType();
        var map = uploadService.presign(req.fileName(), ct);
        String key = map.get("key"), putUrl = map.get("putUrl"), publicUrl = map.get("publicUrl");
        if (key == null || putUrl == null || publicUrl == null) {
            log.error("presign result malformed: {}", map);
            throw new CustomException(ErrorCode.S3_IO_ERROR);
        }
        return new PresignResponse(key, putUrl, publicUrl);
    }

    @PostMapping("/download-urls")
    public Map<String, String> getDownloadUrls(@RequestBody DownloadUrlRequest req) {
        return uploadService.getDownloadUrls(req.imageKeys());
    }
}
