import { apiFetch } from "../../../utils/http";
import type { AuctionDetailResponse } from "../types/auctionDetail";
import type { AuctionCreateRequest, AuctionCreateResponse } from "../types/auction";

// 서버 공통 envelope을 반영한 커스텀 타입
type BidResponse =
    | { success: true }
    | { success: false; data: null; errorCode?: string; message?: string };

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
        if (!res.success) {
            // 서버가 200이어도 success:false면 에러로 처리
            throw new Error(res.message || "입찰에 실패했습니다.");
        }
        return res;
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
