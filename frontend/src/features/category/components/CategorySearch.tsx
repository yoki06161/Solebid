import { useState } from "react";
import type { CategorySearchProps } from "../types/CategorySearchProps";

const CategorySearch = ({
    brands,
    priceRanges,
    sortOptions,
    selectedBrands,
    handleBrandFilter,
    selectedPriceRange,
    setSelectedPriceRange,
    sortBy,
    setSortBy,
}: CategorySearchProps) => {
    const [isPriceDropdownOpen, setIsPriceDropdownOpen] = useState(false);
    const [isSortDropdownOpen, setIsSortDropdownOpen] = useState(false);

    const selectedSortOption = sortOptions.find(option => option.value === sortBy);
    const selectedPriceRangeOption = priceRanges.find(range => range.value === selectedPriceRange);

    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-8">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div>
                    <h3 className="text-sm font-medium text-gray-900 mb-3">
                        브랜드
                    </h3>
                    <div className="flex flex-wrap gap-2">
                        {brands.map((brand) => (
                            <button
                                key={brand}
                                onClick={() => handleBrandFilter(brand)}
                                className={
                                    `px-3 py-1 text-sm rounded-lg border cursor-pointer whitespace-nowrap 
                                    ${selectedBrands.includes(brand)
                                        ? "bg-blue-500 text-white border-blue-500"
                                        : "bg-white text-gray-700 border-gray-300 hover:border-blue-500"
                                    }`
                                }
                            >
                                {brand}
                            </button>
                        ))}
                    </div>
                </div>
                <div>
                    <h3 className="text-sm font-medium text-gray-900 mb-3">
                        가격대
                    </h3>
                    <div className="relative">
                        <button
                            onClick={() => setIsPriceDropdownOpen(!isPriceDropdownOpen)}
                            className="w-full px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer flex justify-between items-center"
                        >
                            <span>
                                {selectedPriceRangeOption ? selectedPriceRangeOption.label : '전체 가격대'}
                            </span>
                            <i className="fas fa-chevron-down text-xs text-gray-400" />
                        </button>
                        {isPriceDropdownOpen && (
                            <div className="absolute right-0 mt-2 w-full bg-white border border-gray-200 rounded-lg shadow-lg z-20">
                                <button
                                    onClick={() => {
                                        setSelectedPriceRange("");
                                        setIsPriceDropdownOpen(false);
                                    }}
                                    className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 cursor-pointer"
                                >
                                    전체 가격대
                                </button>
                                {priceRanges.map((range) => (
                                    <button
                                        key={range.value}
                                        onClick={() => {
                                            setSelectedPriceRange(range.value);
                                            setIsPriceDropdownOpen(false);
                                        }}
                                        className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 cursor-pointer"
                                    >
                                        {range.label}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
                <div>
                    <h3 className="text-sm font-medium text-gray-900 mb-3">
                        정렬
                    </h3>
                    <div className="relative">
                        <button
                            onClick={() => setIsSortDropdownOpen(!isSortDropdownOpen)}
                            className="w-full px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer flex justify-between items-center"
                        >
                            <span>
                                {selectedSortOption ? selectedSortOption.label : ''}
                            </span>
                            <i className="fas fa-chevron-down text-xs text-gray-400" />
                        </button>
                        {isSortDropdownOpen && (
                            <div className="absolute right-0 mt-2 w-full bg-white border border-gray-200 rounded-lg shadow-lg z-20">
                                {sortOptions.map((option) => (
                                    <button
                                        key={option.value}
                                        onClick={() => {
                                            setSortBy(option.value);
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
        </div>
    );
};

export default CategorySearch;