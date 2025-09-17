// src/features/auction/types/auction.ts 임시 파일

/*
export type AuctionCreateRequest = {
    productId: number;
    startPrice: number | string; // 서버 BigDecimal 호환
    endAt: string;               // ISO (e.g., '2025-09-15T21:00')
    startAt?: string | null;
    buyoutPrice?: number | string | null;
    tickSize?: number | string | null;
    extendSeconds?: number | null;
};

export type AuctionCreateResponse = {
    auctionEventId: number;
};
*/

// src/features/auction/types/auction.ts
export interface AuctionCreateRequest {
    productId: number;
    startPrice: number;
    endAt: string;
}
export interface AuctionCreateResponse {
    id: number;
}
