// presign + S3 PUT (테스트용, 실서버 붙일 때 사용)
export type PresignResponse = { key: string; putUrl: string; publicUrl: string };

export async function presignUpload(fileName?: string): Promise<PresignResponse> {
    const res = await fetch("/api/uploads/presign", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fileName }),
    });
    if (!res.ok) throw new Error("presign 요청 실패");
    return res.json();
}

export async function putToS3(putUrl: string, file: File) {
    const r = await fetch(putUrl, {
        method: "PUT",
        headers: { "Content-Type": file.type || "application/octet-stream" },
        body: file,
    });
    if (!r.ok) throw new Error("S3 업로드 실패");
}
