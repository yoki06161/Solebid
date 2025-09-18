/**
 * 파일 경로를 실제 이미지 URL로 변환
 * - http/https로 시작하면 그대로 반환
 * - S3 key면 /api/files/{key} 프록시 경유
 */
// src/utils/toImageUrl.ts
export const toImageUrl = (filePath?: string) =>
    !filePath
        ? undefined
        : filePath.startsWith("http")
            ? filePath
            // encodeURIComponent → encodeURI 로 바꿔야 함
            : `/api/files/${encodeURI(filePath)}`;