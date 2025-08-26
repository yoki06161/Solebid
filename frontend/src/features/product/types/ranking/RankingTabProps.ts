export type Tab = "products" | "sellers" | "bidders";

export interface RankingTabProps {
    activeTab: Tab;
    onTabClick: (tab: Tab) => void;
}