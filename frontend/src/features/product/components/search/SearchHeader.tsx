import { useState } from "react";
import { useClickOutside } from "../../hooks/useClickOutside";
import type { SearchHeaderProps } from "../../types/search/SearchHeaderProps";

const SearchHeader = ({
    searchQuery,
    totalResults,
    sortBy,
    setSortBy,
    sortOptions,
    showFilters,
    setShowFilters
}: SearchHeaderProps) => {
    const [isSortOpen, setIsSortOpen] = useState(false);
    const sortDropdownRef = useClickOutside(() => setIsSortOpen(false));

    const handleSortChange = (option: string) => {
        setSortBy(option);
        setIsSortOpen(false);
    }

    return (
        <div className="mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-6">
                "{searchQuery}"에 대한 검색결과 {totalResults}개
            </h2>
            <div className="flex items-center justify-between">
                <button
                    onClick={() => setShowFilters(!showFilters)}
                    className="md:hidden px-4 py-2 bg-white border border-gray-300 text-gray-700 !rounded-button hover:bg-gray-50">
                    <i className="fas fa-filter mr-2" />
                    필터
                </button>
                <div className="flex items-center space-x-4">
                    <span className="text-sm text-gray-600">
                        정렬:
                    </span>
                    <div
                        className="relative"
                        ref={sortDropdownRef}
                    >
                        <button
                            onClick={() => setIsSortOpen(!isSortOpen)}
                            className="flex items-center justify-between min-w-[120px] bg-white border border-gray-300 px-4 py-2 rounded-lg text-sm focus:outline-none"
                        >
                            <span>
                                {sortBy}
                            </span>
                            <i className="fas fa-chevron-down text-gray-400 text-xs ml-2" />
                        </button>
                        {isSortOpen && (
                            <div className="absolute top-full left-0 mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg z-10">
                                {sortOptions.map((option) => (
                                    <button
                                        key={option}
                                        onClick={() => handleSortChange(option)}
                                        className={
                                            `w-full px-4 py-2 text-left text-sm hover:bg-gray-50 
                                            ${sortBy === option
                                                ? "bg-blue-50 text-blue-600"
                                                : "text-gray-700"
                                            }
                                            `}>
                                        {option}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SearchHeader;