import { http } from "../../../utils/http";

export type PresignResponse = {
    key: string;
    putUrl: string;
    publicUrl?: string;
};

export async function presign(
    fileName: string,
    contentType: string,
    opts?: { userId?: number; token?: string | null }
): Promise<PresignResponse> {
    return http.post<PresignResponse>(
        "/api/uploads/presign",
        { fileName, contentType },
        { userId: opts?.userId ?? null, token: opts?.token ?? null }
    );
}

export async function uploadToS3(
    putUrl: string,
    file: File,
    contentType: string
): Promise<void> {
    const res = await fetch(putUrl, {
        method: "PUT",
        headers: { "Content-Type": contentType },
        body: file,
    });
    if (!res.ok) {
        const msg = `S3 업로드 실패 (status ${res.status})`;
        throw new Error(msg);
    }
}
