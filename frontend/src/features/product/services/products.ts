// 백엔드 상품 생성 API (임시 X-User-Id 지원 -> 후에 변경 예정)
export interface ProductCreatePayload {
    category: string;
    status: string;
    condition: string;
    brand: string;
    size: number; // 220~320
    name: string;
    description: string;
    modelCode?: string;
    colorway?: string;
    releaseDate?: string; // YYYY-MM-DD
    images: Array<{
        filePath: string;   // products/... (S3 key)
        fileName: string;
        sortOrder: number;  // 0..n
        isThumbnail: boolean;
    }>;
}

export async function createProduct(payload: ProductCreatePayload, userId = 1) {
    const res = await fetch("/api/products", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-User-Id": String(userId), // 운영 전 JWT 전환 예정
        },
        body: JSON.stringify(payload),
    });

    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
        const msg =
            data?.message ||
            ({
                THUMBNAIL_DUPLICATED: "썸네일은 1장만 설정할 수 있어요.",
                SORT_ORDER_DUPLICATED: "이미지 정렬 번호가 중복되었습니다.",
                INVALID_IMAGE_KEY: "이미지 경로가 올바르지 않습니다. (products/...)",
                INVALID_INPUT_VALUE: "입력값을 확인해 주세요.",
                UNAUTHORIZED: "로그인이 필요합니다.",
            } as Record<string, string>)[data?.errorCode] ||
            "상품 등록 실패";
        throw new Error(msg);
    }
    return data as { productId: number };
}
