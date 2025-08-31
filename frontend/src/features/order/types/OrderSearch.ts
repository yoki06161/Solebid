export interface OrderSearchProps {
    searchQuery: string;
    setSearchQuery: (query: string) => void;
    periods: string[];
    selectedPeriod: string;
    setSelectedPeriod: (period: string) => void;
    statuses: string[];
    selectedStatus: string;
    setSelectedStatus: (status: string) => void;
}