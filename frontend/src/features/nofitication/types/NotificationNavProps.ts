export interface NotificationNavProps {
    tabs: string[];
    activeTab: string;
    onTabChange: (tab: string) => void;
}