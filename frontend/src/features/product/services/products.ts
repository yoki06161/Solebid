import { http } from "../../../utils/http";
import type {
    ProductCreatePayload,
    CreateProductResponse,
    FinalizeImagesResponse,
} from "../types/product";

export async function createProduct(
    payload: ProductCreatePayload,
    opts?: { userId?: number; token?: string | null }
): Promise<CreateProductResponse> {
    return http.post<CreateProductResponse>("/api/products", payload, {
        userId: opts?.userId ?? null,
        token: opts?.token ?? null,
    });
}

export async function finalizeImages(
    productId: number,
    opts?: { userId?: number; token?: string | null }
): Promise<FinalizeImagesResponse> {
    // http 구현이 빈 본문을 허용하지 않으면 {} 로 보내세요
    return http.post<FinalizeImagesResponse>(
        `/api/products/${productId}/finalize-images`,
        {}, // 안전하게 빈 JSON
        { userId: opts?.userId ?? null, token: opts?.token ?? null }
    );
}
