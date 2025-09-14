import {apiFetch} from "../../../utils/apiFetch";
import type {ApiResponse} from "../../user/types/AuthTypes";
import type {AuctionItem} from "../types/AuctionItem";

export async function getProducts(): Promise<ApiResponse<AuctionItem[]>> {
    const data = await apiFetch<ApiResponse<AuctionItem[]>>('/api/products');

    if (!data.success) {
        throw new Error(data.message || 'API returned an error');
    }

    return data;
}
