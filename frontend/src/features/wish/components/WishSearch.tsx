import { useState } from "react";
import type { WishSearchProps } from "../types/WishSearchProps";
import { sortOptions } from "./mockData";

const WishSearch = ({
    categories,
    selectedCategory,
    onCategoryChange,
    sortBy,
    onSortChange,
}: WishSearchProps) => {
    const [isCategoryDropdownOpen, setIsCategoryDropdownOpen] = useState(false);
    const [isSortDropdownOpen, setIsSortDropdownOpen] = useState(false);
    const selectedSortOption = sortOptions.find(option => option.value === sortBy);
    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <div className="flex justify-between items-center">
                <div className="relative">
                    <button
                        onClick={() => setIsCategoryDropdownOpen(!isCategoryDropdownOpen)}
                        className="appearance-none bg-white border border-gray-300 rounded-lg px-4 py-2 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer flex justify-between items-center w-32"
                    >
                        <span>
                            {selectedCategory}
                        </span>
                        <i className="fas fa-chevron-down text-xs text-gray-400" />
                    </button>
                    {isCategoryDropdownOpen && (
                        <div className="absolute right-0 mt-2 w-32 bg-white border border-gray-200 rounded-lg shadow-lg z-20">
                            {categories.map((category) => (
                                <button
                                    key={category}
                                    onClick={() => {
                                        onCategoryChange(category);
                                        setIsCategoryDropdownOpen(false);
                                    }}
                                    className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 cursor-pointer"
                                >
                                    {category}
                                </button>
                            ))}
                        </div>
                    )}
                </div>
                <div className="relative">
                    <button
                        onClick={() => setIsSortDropdownOpen(!isSortDropdownOpen)}
                        className="appearance-none bg-white border border-gray-300 rounded-lg px-4 py-2 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer flex justify-between items-center w-36"
                    >
                        <span>
                            {selectedSortOption ? selectedSortOption.label : ''}
                        </span>
                        <i className="fas fa-chevron-down text-xs text-gray-400" />
                    </button>
                    {isSortDropdownOpen && (
                        <div className="absolute right-0 mt-2 w-36 bg-white border border-gray-200 rounded-lg shadow-lg z-20">
                            {sortOptions.map((option) => (
                                <button
                                    key={option.value}
                                    onClick={() => {
                                        onSortChange(option.value);
                                        setIsSortDropdownOpen(false);
                                    }}
                                    className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 cursor-pointer"
                                >
                                    {option.label}
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default WishSearch;