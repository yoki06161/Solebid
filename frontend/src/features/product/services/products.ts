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