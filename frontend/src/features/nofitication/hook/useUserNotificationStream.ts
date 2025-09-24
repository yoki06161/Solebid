import { useEffect, useRef } from "react";
import { NotificationApi } from "../api";

// 공통 SSE 이벤트 리스너 타입
type Listener<T> = (data: T) => void;

interface HelloEvent { message: string }
interface BadgeEvent { unreadCount: number }
interface OutbidEvent {
    auctionEventId: number;
    productName: string;
    currentPrice: string;
    myBid: string;
}
interface AuctionWonEvent {
    notificationId: number;
    auctionEventId: number;
    productName: string;
    finalPrice: string;
}
interface AuctionLostEvent {
    notificationId: number;
    auctionEventId: number;
    productName: string;
    finalPrice: string;
}

interface Options {
    onHello?: Listener<HelloEvent>;
    onBadge?: Listener<BadgeEvent>;
    onOutbid?: Listener<OutbidEvent>;
    onAuctionWon?: Listener<AuctionWonEvent>;
    onAuctionLost?: Listener<AuctionLostEvent>;
    reconnect?: boolean;
}

export function useUserNotificationStream({
                                              onHello,
                                              onBadge,
                                              onOutbid,
                                              onAuctionWon,
                                              onAuctionLost,
                                              reconnect = true,
                                          }: Options) {
    const esRef = useRef<EventSource | null>(null);
    const retryRef = useRef(0);
    const timerRef = useRef<number | null>(null);

    useEffect(() => {
        let stopped = false;

        const connect = async () => {
            try {
                const { token } = await NotificationApi.issueStreamToken();
                if (stopped) return;

                const es = new EventSource(
                    `/api/stream/notifications?token=${encodeURIComponent(token)}`,
                    { withCredentials: true }
                );

                esRef.current = es;
                retryRef.current = 0;

                es.addEventListener("hello", (e: MessageEvent) => {
                    try {
                        const data: HelloEvent = JSON.parse(e.data);
                        onHello?.(data);
                    } catch {}
                });
                es.addEventListener("badge", (e: MessageEvent) => {
                    try {
                        const data: BadgeEvent = JSON.parse(e.data);
                        onBadge?.(data);
                    } catch {}
                });
                es.addEventListener("outbid", (e: MessageEvent) => {
                    try {
                        const data: OutbidEvent = JSON.parse(e.data);
                        onOutbid?.(data);
                    } catch {}
                });
                es.addEventListener("auctionWon", (e: MessageEvent) => {
                    try {
                        const data: AuctionWonEvent = JSON.parse(e.data);
                        onAuctionWon?.(data);
                    } catch {}
                });
                es.addEventListener("auctionLost", (e: MessageEvent) => {
                    try {
                        const data: AuctionLostEvent = JSON.parse(e.data);
                        onAuctionLost?.(data);
                    } catch {}
                });

                es.addEventListener("error", () => {
                    es.close();
                    if (reconnect && !stopped) scheduleReconnect();
                });
            } catch {
                if (reconnect && !stopped) scheduleReconnect();
            }
        };

        const scheduleReconnect = () => {
            const delay = Math.min(30000, 1000 * Math.pow(2, retryRef.current++));
            if (timerRef.current) window.clearTimeout(timerRef.current);
            timerRef.current = window.setTimeout(connect, delay);
        };

        connect();

        return () => {
            stopped = true;
            if (timerRef.current) window.clearTimeout(timerRef.current);
            esRef.current?.close();
        };
    }, [onHello, onBadge, onOutbid, onAuctionWon, onAuctionLost, reconnect]);
}
