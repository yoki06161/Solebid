import { useContext } from "react";
import { NotificationStreamContext } from "./NotificationStreamContext";

export function useNotificationStream() {
    const ctx = useContext(NotificationStreamContext);
    if (!ctx) throw new Error("useNotificationStream must be used within NotificationStreamProvider");
    return ctx;
}
