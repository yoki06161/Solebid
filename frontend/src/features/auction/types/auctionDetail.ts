export type AuctionStatus = "READY" | "LIVE" | "ENDED" | "SETTLED" | "CANCELED";

export interface AuctionDetailResponse {
    auctionEventId: number;
    status: AuctionStatus;
    startPrice: number;
    buyoutPrice: number | null;
    currentPrice: number;
    tickSize: number;
    startAt: string;    // "2025-09-17T14:56:29.5646"
    endAt: string;      // "2025-09-20T06:00:00"
    extendSeconds: number | null;
    viewCount: number;
    isBlind: boolean;
    version: number;
    product: {
        productId: number;
        name: string;
        brand: string;
        category: string;
        size: number;
        condition: string;
        modelCode: string | null;
        colorway: string | null;
        releaseDate: string; // YYYY-MM-DD
        images: Array<{
            filePath: string;
            isThumbnail: boolean;
            sortOrder: number;
        }>;
    };
}
