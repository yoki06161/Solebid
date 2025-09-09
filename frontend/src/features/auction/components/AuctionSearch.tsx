import { useState } from "react";
import type { AuctionSearchProps } from "../types/AuctionSearchProps";
import {Link} from "react-router-dom";

const AuctionSearch = ({
    categories,
    selectedCategory,
    onCategoryChange,
    sortOptions,
    sortOption,
    onSortChange,
    priceRange,
    onPriceChange,
}: AuctionSearchProps) => {
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    return (
        <div className="bg-white shadow-sm rounded-lg p-6 mb-8">
            <div className="flex flex-wrap items-center justify-between gap-4">
                <div className="flex space-x-2">
                    {categories.map(category => (
                        <button
                            key={category}
                            onClick={() => onCategoryChange(category)}
                            className={
                                `px-4 py-2 rounded-lg whitespace-nowrap cursor-pointer shadow-sm
                                ${selectedCategory === category
                                    ? 'bg-blue-500 text-white'
                                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                }`
                            }
                        >
                            {category}
                        </button>
                    ))}
                </div>
                <div className="flex items-center space-x-4">
                    <div className="bg-white border border-gray-300 rounded-lg">
                        <Link
                            to="/products/new"
                            className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 cursor-pointer"
                        >상품 등록
                        </Link>
                    </div>
                    <div className="relative">
                        <button
                            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                            className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 flex items-center space-x-2 cursor-pointer"
                        >
                            <span>
                                {sortOption}
                            </span>
                            <i className="fas fa-chevron-down text-sm" />
                        </button>
                        {isDropdownOpen && (
                            <div className="absolute right-0 mt-2 w-40 bg-white border border-gray-200 rounded-lg shadow-lg z-20">
                                {sortOptions.map(option => (
                                    <button
                                        key={option}
                                        onClick={() => {
                                            onSortChange(option);
                                            setIsDropdownOpen(false);
                                        }}
                                        className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 cursor-pointer"
                                    >
                                        {option}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>
                    <div className="w-64">
                        <input
                            type="range"
                            min="0"
                            max="1000000"
                            step="10000"
                            value={priceRange[1]}
                            onChange={(e) => onPriceChange([0, parseInt(e.target.value)])}
                            className="w-full"
                        />
                        <div className="text-sm text-gray-600 mt-1">
                            최대 가격: ₩{priceRange[1].toLocaleString()}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AuctionSearch;