package com.sesac.solbid.controller;

import com.sesac.solbid.dto.upload.request.PresignRequest;
import com.sesac.solbid.dto.upload.response.PresignResponse;
import com.sesac.solbid.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;

    /*
     * 파일 업로드용 Presigned URL 발급
     * POST /presign
     * URL 생성 후 저장
     * 서버 { key, putUrl, publicUrl } 응답
     * 업로드 URL만 발급
     * */
    @PostMapping("/presign")
    public PresignResponse presign(@RequestBody PresignRequest req){
        String ct = (req.contentType() == null || req.contentType().isBlank())
                ? "image/jpeg" : req.contentType();
        var map = uploadService.presign(req.fileName(), ct);
        return new PresignResponse(map.get("key"), map.get("putUrl"), map.get("publicUrl"));
    }
}
