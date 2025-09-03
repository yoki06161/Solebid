export interface SearchHeaderProps {
    searchQuery: string;
    setSearchQuery: (query: string) => void;
    handleClearSearch: () => void;
    handleCloseModal: () => void;
}