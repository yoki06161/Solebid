import React, { useEffect, useMemo, useState } from "react";
import { useToast } from "../toast/ToastContext";
import { NotificationStreamContext, type NotificationCtx } from "./NotificationStreamContext";
import {NotificationApi} from "../../features/nofitication/api";
import {useUserNotificationStream} from "../../features/nofitication/hook/useUserNotificationStream.ts";

export default function NotificationStreamProvider({ children }: { children: React.ReactNode }) {
    const toast = useToast();
    const [unread, setUnread] = useState(0);

    const refreshUnread = async () => {
        try {
            const r = await NotificationApi.unreadCount();
            setUnread(r.count ?? 0);
        } catch {
            // noop
        }
    };

    useEffect(() => {
        void refreshUnread();
    }, []);

    useUserNotificationStream({
        onBadge: (d) => {
            if (typeof d?.unreadCount === "number") setUnread(d.unreadCount);
        },
        onOutbid: (d) => {
            toast.warning(
                `입찰가 갱신!\n${d.productName}\n현재가: ${d.currentPrice}\n내 입찰가: ${d.myBid}`,
                { autoClose: 7000 }
            );
        },
    });

    const value = useMemo<NotificationCtx>(() => ({ unread, refreshUnread }), [unread]);

    return <NotificationStreamContext.Provider value={value}>{children}</NotificationStreamContext.Provider>;
}
