export interface NotificationSearchProps {
    sortBy: string;
    onSortChange: (value: string) => void;
    searchQuery: string;
    onSearchChange: (value: string) => void;
}