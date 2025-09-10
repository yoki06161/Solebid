import type { ApiResponse } from "../../user/types/AuthTypes";
import type { AuctionItem } from "../types/AuctionItem";

export async function getProducts(): Promise<ApiResponse<AuctionItem[]>> {
  const res = await fetch('/api/products', {
    method: 'GET',
    headers: { Accept: 'application/json' },
    credentials: 'include',
  });

  if (!res.ok) {
    const errorResponse = await res.json().catch(() => ({}));
    throw new Error(errorResponse.message || `HTTP error! status: ${res.status}`);
  }

  const data: ApiResponse<AuctionItem[]> = await res.json();

  if (!data.success) {
    throw new Error(data.message || 'API returned an error');
  }

  return data;
}