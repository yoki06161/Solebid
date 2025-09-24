import { http } from "../../../utils/http";

export interface ServerNotification {
    notificationId: number;
    notificationType: "BID" | "OUTBID" | "WIN" | "INQUIRY_ANSWER" | "ETC";
    title: string;
    content: string;
    linkUrl?: string | null;
    isRead: boolean;
    createAt: string; // ISO
}

export interface PageResp<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    last: boolean;
}

export const NotificationApi = {
    unreadCount: () =>
        http.get<{ count: number }>("/api/notifications/unread-count"),

    list: (page = 0, size = 20) =>
        http.get<PageResp<ServerNotification>>(`/api/notifications?page=${page}&size=${size}`),

    markRead: (id: number) =>
        http.post<{ success: boolean }>(`/api/notifications/${id}/read`),

    markAllRead: () =>
        http.post<{ success: boolean }>("/api/notifications/read-all"),

    issueStreamToken: () =>
        http.post<{ token: string }>("/api/stream/token"),
};
