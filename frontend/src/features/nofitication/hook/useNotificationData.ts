import { useCallback, useEffect, useState } from "react";
import { NotificationApi } from "../api";
import type { ServerNotification, PageResp } from "../api";

export function useNotificationData() {
    const [page, setPage] = useState(0);
    const [size] = useState(20);
    const [list, setList] = useState<ServerNotification[]>([]);
    const [hasMore, setHasMore] = useState(true);
    const [loading, setLoading] = useState(false);
    const [unread, setUnread] = useState(0);

    const refreshUnread = useCallback(async () => {
        const res = await NotificationApi.unreadCount();
        setUnread(res.count ?? 0);
    }, []);

    const loadFirst = useCallback(async () => {
        setLoading(true);
        try {
            const res: PageResp<ServerNotification> = await NotificationApi.list(0, size);
            setList(res.content);
            setHasMore(!res.last);
            setPage(0);
            await refreshUnread();
        } finally {
            setLoading(false);
        }
    }, [size, refreshUnread]);

    const loadMore = useCallback(async () => {
        if (!hasMore || loading) return;
        setLoading(true);
        try {
            const next = page + 1;
            const res = await NotificationApi.list(next, size);
            setList((prev) => [...prev, ...res.content]);
            setHasMore(!res.last);
            setPage(next);
        } finally {
            setLoading(false);
        }
    }, [page, size, hasMore, loading]);

    const markRead = useCallback(async (id: number) => {
        await NotificationApi.markRead(id);
        setList((prev) =>
            prev.map((n) => (n.notificationId === id ? { ...n, isRead: true } : n)),
        );
        await refreshUnread();
    }, [refreshUnread]);

    const markAllRead = useCallback(async () => {
        await NotificationApi.markAllRead();
        setList((prev) => prev.map((n) => ({ ...n, isRead: true })));
        await refreshUnread();
    }, [refreshUnread]);

    useEffect(() => {
        loadFirst();
    }, [loadFirst]);

    return { list, hasMore, loading, unread, loadMore, loadFirst, markRead, markAllRead, setUnread };
}
