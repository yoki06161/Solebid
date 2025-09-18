import { http } from '../../../utils/http';
import type { AuctionDetailResponse } from "../types/auctionDetail";
import type { AuctionCreateRequest, AuctionCreateResponse } from '../types/auction';
import { apiFetch } from "../../../utils/http";


export async function createAuction(payload: AuctionCreateRequest) {
    return http.post<AuctionCreateResponse>('/api/auctions', payload);
}

export const AuctionsApi = {
    getDetail(auctionId: number) {
        return apiFetch<AuctionDetailResponse>(`/api/auctions/${auctionId}`);
    },
};