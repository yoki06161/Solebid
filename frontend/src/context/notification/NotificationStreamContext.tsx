import { createContext } from "react";

export type NotificationCtx = {
    unread: number;
    refreshUnread: () => Promise<void>;
};

export const NotificationStreamContext = createContext<NotificationCtx | null>(null);
