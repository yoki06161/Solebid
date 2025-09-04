export interface TransactionSearchProps {
    searchQuery: string;
    setSearchQuery: (query: string) => void;
    showDateDropdown: boolean;
    setShowDateDropdown: (show: boolean) => void;
    setSelectedDateFilter: (filter: string) => void;
    showStatusDropdown: boolean;
    setShowStatusDropdown: (show: boolean) => void;
    setSelectedStatusFilter: (filter: string) => void;
    selectedDateFilter: string;
    selectedStatusFilter: string;
}