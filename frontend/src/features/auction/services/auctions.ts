import { apiFetch } from "../../../utils/http";
import type { AuctionDetailResponse } from "../types/auctionDetail";
import type { AuctionCreateRequest, AuctionCreateResponse } from "../types/auction";

type BidResponse = { success: true } | { success: false; message?: string };

export const AuctionsApi = {
    getDetail(auctionId: number) {
        return apiFetch<AuctionDetailResponse>(`/api/auctions/${auctionId}`);
    },

    async placeBid(auctionId: number, amount: number, idempotencyKey: string) {
        const res = await apiFetch<BidResponse>(`/api/auctions/${auctionId}/bids`, {
            method: "POST",
            headers: { "X-Idempotent-Key": idempotencyKey },
            body: { amount, idempotencyKey },
        });

        if (res?.success === true) return true;

        const message = res && "message" in res && typeof res.message === "string"
            ? res.message
            : "입찰에 실패했습니다.";
        throw new Error(message);
    },

    create(payload: AuctionCreateRequest) {
        return apiFetch<AuctionCreateResponse>(`/api/auctions`, {
            method: "POST",
            body: payload,
        });
    },
};

// 개인 알림 SSE 토큰
export const StreamApi = {
    issueToken() {
        return apiFetch<{ token: string }>(`/api/stream/token`, { method: "POST" });
    },
};
