import { useEffect, useRef } from "react";

type Handlers = {
    onBid?: (p: { currentPrice: number; version: number }) => void;
    onExtended?: (p: { endAt: string; extendSeconds: number | null }) => void;
    onStatus?: (p: { status: string }) => void;
};

export function useAuctionStream(auctionId?: number, handlers: Handlers = {}) {
    const ref = useRef<EventSource | null>(null);

    useEffect(() => {
        if (!auctionId) return;

        const url = `/api/auctions/${auctionId}/stream`;
        const es = new EventSource(url, { withCredentials: true });
        ref.current = es;

        es.addEventListener("hello", () => { /* 연결 확인용 */ });

        es.addEventListener("bid", (ev) => {
            try {
                const data = JSON.parse((ev as MessageEvent).data);
                handlers.onBid?.({
                    currentPrice: Number(data.currentPrice),
                    version: Number(data.version),
                });
            } catch {}
        });

        es.addEventListener("extended", (ev) => {
            try {
                const data = JSON.parse((ev as MessageEvent).data);
                handlers.onExtended?.({
                    endAt: String(data.endAt),
                    extendSeconds: data.extendSeconds ?? null,
                });
            } catch {}
        });

        es.addEventListener("status", (ev) => {
            try {
                const data = JSON.parse((ev as MessageEvent).data);
                handlers.onStatus?.({ status: String(data.status) });
            } catch {}
        });

        es.onerror = () => { /* 재시도는 브라우저가 자동 처리 */ };

        return () => {
            es.close();
            ref.current = null;
        };
    }, [auctionId]);
}
