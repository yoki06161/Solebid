export interface SearchHistoryProps {
    recentSearches: string[];
    handleRecentSearchClick: (search: string) => void;
    handleRemoveRecentSearch: (search: string) => void;
    handleClearAllRecentSearches: () => void;
}