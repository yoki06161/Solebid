import { useEffect, useMemo, useState } from "react";
import { AuctionsApi } from "../services/auctions";
import type { AuctionDetailResponse } from "../types/auctionDetail";

export function useAuctionDetail(auctionId?: number) {
    const validId = useMemo(
        () => (typeof auctionId === "number" && Number.isFinite(auctionId) ? auctionId : undefined),
        [auctionId]
    );

    const [data, setData] = useState<AuctionDetailResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);

    useEffect(() => {
        if (!validId) {
            setErr("유효하지 않은 경매 ID입니다.");
            setLoading(false);
            return;
        }
        let cancel = false;
        (async () => {
            try {
                setLoading(true);
                setErr(null);
                const res = await AuctionsApi.getDetail(validId);
                if (!cancel) setData(res);
            } catch (e: unknown) {
                if (!cancel) setErr(e instanceof Error ? e.message : "불러오기 실패");
            } finally {
                if (!cancel) setLoading(false);
            }
        })();
        return () => {
            cancel = true;
        };
    }, [validId]);

    return { data, loading, err };
}
