//import { http } from "../../../utils/http";

/*
export interface AuctionCreatePayload {
    productId: number;
    startPrice: number;           // 원화 정수(예: 10000)
    endAt: string;                // ISO string (예: 2025-09-20T04:30:00.000Z)
    buyoutPrice?: number | null;  //
    tickSize?: number | null;     // 기본 1.00
    extendSeconds?: number | null;// 기본 30
}

export interface AuctionCreateResponse {
    auctionEventId: number;
}

export async function createAuctionEvent(
    payload: AuctionCreatePayload,
    opts?: { userId?: number; token?: string | null }
): Promise<AuctionCreateResponse> {
    return http.post<AuctionCreateResponse>("/api/auctions", payload, {
        userId: opts?.userId ?? null,
        token: opts?.token ?? null,
    });
}
*/

// src/features/auction/services/auctions.ts
import { http } from '../../../utils/http';
import type { AuctionCreateRequest, AuctionCreateResponse } from '../types/auction';

export async function createAuction(payload: AuctionCreateRequest) {
    // 쿠키로 인증
    return http.post<AuctionCreateResponse>('/api/auctions', payload);
}
