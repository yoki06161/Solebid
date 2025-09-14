import {apiFetch} from "../../../utils/apiFetch";
import type {ApiResponse} from "../../user/types/AuthTypes";
import type {AuctionItem} from "../types/AuctionItem";

export async function getProducts(params?: { sortBy?: string, limit?: number }): Promise<ApiResponse<AuctionItem[]>> {
    const queryParams: Record<string, string> = {
        ...(params?.sortBy && {sortBy: params.sortBy}),
        ...(params?.limit !== undefined && {limit: String(params.limit)}),
    };

    const query = new URLSearchParams(queryParams);
    const queryString = query.toString();
    const queryWithPrefix = queryString ? `?${queryString}` : '';
    const url = `/api/products${queryWithPrefix}`;

    const data = await apiFetch<ApiResponse<AuctionItem[]>>(url);

    if (!data.success) {
        throw new Error(data.message || 'API returned an error');
    }

    return data;
}
