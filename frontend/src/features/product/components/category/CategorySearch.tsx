import type { CategorySearchProps } from "../../types/category/CategorySearchProps";

const CategorySearch: React.FC<CategorySearchProps> = ({
    brands,
    priceRanges,
    sortOptions,
    selectedBrands,
    handleBrandFilter,
    selectedPriceRange,
    setSelectedPriceRange,
    sortBy,
    setSortBy,
}) => {
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
                                    `px-3 py-1 text-sm !rounded-button border cursor-pointer whitespace-nowrap 
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
                        <select
                            value={selectedPriceRange}
                            onChange={(e) => setSelectedPriceRange(e.target.value)}
                            className="w-full px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer appearance-none"
                        >
                            <option value="">
                                전체 가격대
                            </option>
                            {priceRanges.map((range) => (
                                <option
                                    key={range.value}
                                    value={range.value}>
                                    {range.label}
                                </option>
                            ))}
                        </select>
                        <i className="fas fa-chevron-down absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-xs pointer-events-none" />
                    </div>
                </div>
                <div>
                    <h3 className="text-sm font-medium text-gray-900 mb-3">
                        정렬
                    </h3>
                    <div className="relative">
                        <select
                            value={sortBy}
                            onChange={(e) => setSortBy(e.target.value)}
                            className="w-full px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer appearance-none"
                        >
                            {sortOptions.map((option) => (
                                <option
                                    key={option.value}
                                    value={option.value}
                                >
                                    {option.label}
                                </option>
                            ))}
                        </select>
                        <i className="fas fa-chevron-down absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-xs pointer-events-none" />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CategorySearch;