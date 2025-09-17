/*
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
*/

import { apiFetch } from '../../../utils/apiFetch';
import type {
    ProductCreatePayload,
    CreateProductResponse,
    FinalizeImagesResponse,
} from '../types/product';

// 서버 응답이 { id } 또는 { productId } 일 수 있으므로 정규화해서 반환
export async function createProduct(payload: ProductCreatePayload): Promise<CreateProductResponse> {
    const res = await apiFetch<{ id?: number; productId?: number }>('/api/products', {
        method: 'POST',
        json: payload,
    });
    const productId = res.productId ?? res.id;
    if (!productId) throw new Error('서버 응답에 productId/id가 없습니다.');
    return { productId };
}

export async function finalizeImages(productId: number): Promise<FinalizeImagesResponse> {
    return apiFetch<FinalizeImagesResponse>(`/api/products/${productId}/finalize-images`, {
        method: 'POST',
        json: {}, // 빈 JSON 안전하게 전송
    });
}