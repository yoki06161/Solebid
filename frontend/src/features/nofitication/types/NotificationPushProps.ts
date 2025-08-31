import type { Notification } from "./Notification";

export interface NotificationPushProps {
    notification: Notification;
    onClose: () => void;
}