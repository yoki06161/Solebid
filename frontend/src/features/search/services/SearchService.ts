import {apiFetch} from '../../../utils/apiFetch';
import type {ApiResponse} from '../../user/types/AuthTypes';
import type {AuctionItem} from '../../auction/types/AuctionItem';

export async function searchProducts(keyword: string): Promise<ApiResponse<AuctionItem[]>> {
    const url = `/api/products/search?keyword=${encodeURIComponent(keyword)}`;

    const data = await apiFetch<ApiResponse<AuctionItem[]>>(url);

    if (!data.success) {
        throw new Error(data.message || 'API returned an error');
    }

    return data;
}
