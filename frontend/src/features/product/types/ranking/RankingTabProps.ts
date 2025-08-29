export type Tab = "products" | "sellers" | "bidders";

export interface RankingTabProps {
    activeTab: string;
    onTabClick: (tab: string) => void;
}