import {useEffect, useMemo, useRef, useState} from "react";
import { AuctionsApi } from "../services/auctions";
import type { AuctionDetailResponse } from "../types/auctionDetail";

type UseAuctionDetailReturn = {
    data: AuctionDetailResponse | null;
    setData: React.Dispatch<React.SetStateAction<AuctionDetailResponse | null>>;
    loading: boolean;
    err: string | null;
};

export function useAuctionDetail(auctionId?: number): UseAuctionDetailReturn {
    const validId = useMemo(
        () => (typeof auctionId === "number" && Number.isFinite(auctionId) ? auctionId : undefined),
        [auctionId]
    );

    const [data, setData] = useState<AuctionDetailResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);

    // 최신 요청만 반영되도록 요청 번호 추적
    const reqSeq = useRef(0);

    useEffect(() => {
        if (!validId) {
            setErr("유효하지 않은 경매 ID입니다.");
            setData(null);
            setLoading(false);
            return;
        }

        // ID 바뀌면 초기화
        setData(null);
        setErr(null);
        setLoading(true);

        const seq = ++reqSeq.current;
        const ac = new AbortController();

        (async () => {
            try {
                const res = await AuctionsApi.getDetail(validId);
                if (!ac.signal.aborted && seq === reqSeq.current) {
                    setData(res);
                }
            } catch (e: unknown) {
                if (!ac.signal.aborted && seq === reqSeq.current) {
                    setErr(e instanceof Error ? e.message : "불러오기 실패");
                }
            } finally {
                if (!ac.signal.aborted && seq === reqSeq.current) {
                    setLoading(false);
                }
            }
        })();

        return () => {
            ac.abort();
        };
    }, [validId]);

    return { data, setData, loading, err };
}
