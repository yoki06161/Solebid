import type { SearchHistoryProps } from '../types/SearchHistoryProps';

const SearchHistory = ({
    recentSearches,
    handleRecentSearchClick,
    handleRemoveRecentSearch,
    handleClearAllRecentSearches,
}: SearchHistoryProps) => {
    return (
        <div className="px-4 py-6">
            <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold">
                    최근 검색어
                </h3>
                <button
                    onClick={handleClearAllRecentSearches}
                    className="text-sm text-gray-500 hover:text-gray-700 cursor-pointer"
                >
                    전체 삭제
                </button>
            </div>
            <div className="flex flex-wrap gap-2">
                {recentSearches.map((search) => (
                    <div
                        key={search}
                        className="flex items-center bg-gray-100 rounded-full px-3 py-2 text-sm"
                    >
                        <button
                            onClick={() => handleRecentSearchClick(search)}
                            className="hover:text-black"
                        >
                            {search}
                        </button>
                        <button
                            onClick={() => handleRemoveRecentSearch(search)}
                            className="ml-2 text-gray-400 hover:text-gray-600 cursor-pointer"
                        >
                            <i className="fas fa-times text-xs" />
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default SearchHistory;
