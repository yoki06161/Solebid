import { safeJson, throwHttpError, type ErrorResponse } from "../../../utils/http";

export type PresignRequest = { fileName: string; contentType: string };
export type PresignResponse = { key: string; putUrl: string; publicUrl: string };

export async function presignUpload(req: PresignRequest): Promise<PresignResponse> {
    const r = await fetch("/api/uploads/presign", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(req),
    });

    const body = await safeJson<PresignResponse | ErrorResponse>(r);
    if (!r.ok) throwHttpError(body as ErrorResponse | null, "업로드 URL 발급 실패");

    if (!body || !("key" in body)) {
        throw new Error("서버 응답 형식이 올바르지 않습니다. (key 없음)");
    }
    return body as PresignResponse;
}

export async function putToS3(putUrl: string, file: File, contentType?: string): Promise<void> {
    const r = await fetch(putUrl, {
        method: "PUT",
        headers: { "Content-Type": contentType || file.type || "image/jpeg" },
        body: file,
    });
    if (!r.ok) throw new Error("S3 업로드 실패");
}

export { presign, uploadToS3 } from "../../upload/services/uploads";
