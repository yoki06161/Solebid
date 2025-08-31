import type { Notification } from "./Notification";

export interface NotificationListProps {
    notifications: Notification[];
    onNavigate: (id: number) => void;
}