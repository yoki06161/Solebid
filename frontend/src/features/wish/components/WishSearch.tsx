import type { WishSearchProps } from "../../types/wish/WishSearchProps";

const WishSearch: React.FC<WishSearchProps> = ({
    categories,
    selectedCategory,
    onCategoryChange,
    sortBy,
    onSortChange,
}) => (
    <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
        <div className="flex justify-between items-center">
            <div className="relative">
                <select
                    value={selectedCategory}
                    onChange={(e) => onCategoryChange(e.target.value)}
                    className="appearance-none bg-white border border-gray-300 rounded-lg px-4 py-2 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
                >
                    {categories.map((category) => (
                        <option
                            key={category}
                            value={category}
                        >
                            {category}
                        </option>
                    ))}
                </select>
                <i className="fas fa-chevron-down absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-xs pointer-events-none" />
            </div>
            <div className="relative">
                <select
                    value={sortBy}
                    onChange={(e) => onSortChange(e.target.value)}
                    className="appearance-none bg-white border border-gray-300 rounded-lg px-4 py-2 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
                >
                    <option value="latest">최근 등록순</option>
                    <option value="oldest">오래된순</option>
                    <option value="priceHigh">가격 높은순</option>
                    <option value="priceLow">가격 낮은순</option>
                </select>
                <i className="fas fa-chevron-down absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-xs pointer-events-none" />
            </div>
        </div>
    </div>
);

export default WishSearch;