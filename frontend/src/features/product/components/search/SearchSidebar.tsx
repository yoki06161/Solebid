import type { SearchSidebarProps } from "../../types/search/SearchSidebarProps";

const SearchSidebar = ({
    showFilters,
    priceRange,
    setPriceRange,
    brands,
    selectedBrands,
    handleBrandChange,
    sizes,
    selectedSizes,
    handleSizeChange,
    resetFilters,
}: SearchSidebarProps) => {
    return (
        <div className=
            {
                `w-64 bg-white rounded-lg shadow-sm p-6 h-fit 
            ${showFilters
                    ? "block"
                    : "hidden md:block"}
            `}>
            <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-semibold text-gray-900">
                    필터
                </h3>
                <button
                    onClick={resetFilters}
                    className="text-sm text-blue-600 hover:text-blue-800">
                    초기화
                </button>
            </div>
            <div className="mb-8">
                <h4 className="text-sm font-medium text-gray-900 mb-4">
                    가격대
                </h4>
                <div className="flex items-center justify-between text-sm text-gray-600">
                    <span>
                        ₩{priceRange[0].toLocaleString()}
                    </span>
                    <span>
                        ₩{priceRange[1].toLocaleString()}
                    </span>
                </div>
                <input
                    type="range"
                    min="0"
                    max="500000"
                    step="10000"
                    value={priceRange[1]}
                    onChange={(e) => setPriceRange([priceRange[0], parseInt(e.target.value)])}
                    className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer mt-4"
                />
            </div>
            <div className="mb-8">
                <h4 className="text-sm font-medium text-gray-900 mb-4">
                    브랜드
                </h4>
                <div className="space-y-3">
                    {brands.map((brand) => (
                        <label
                            key={brand}
                            className="flex items-center cursor-pointer">
                            <input
                                type="checkbox"
                                checked={selectedBrands.includes(brand)}
                                onChange={() => handleBrandChange(brand)}
                                className="mr-3 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                            />
                            <span className="text-sm text-gray-700">
                                {brand}
                            </span>
                        </label>
                    ))}
                </div>
            </div>
            <div className="mb-8">
                <h4 className="text-sm font-medium text-gray-900 mb-4">
                    사이즈
                </h4>
                <div className="grid grid-cols-3 gap-2">
                    {sizes.map((size) => (
                        <button
                            key={size}
                            onClick={() => handleSizeChange(size)}
                            className={
                                `px-3 py-2 text-sm rounded-lg border
                                 ${selectedSizes.includes(size)
                                    ? "bg-blue-500 text-white border-blue-500"
                                    : "bg-white text-gray-700 border-gray-300 hover:bg-gray-50"}
                              `}>
                            {size}
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default SearchSidebar;