import type { Notification } from "./Notification";

export interface NotificationItemProps {
    notification: Notification;
    onNavigate: (id: number) => void;
}