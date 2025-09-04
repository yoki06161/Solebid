import type { CategoryItemProps } from "../types/CategoryItemProps";

const CategoryItem = ({ category }: CategoryItemProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow overflow-hidden cursor-pointer">
            <div className="aspect-square overflow-hidden">
                <img
                    src={category.image}
                    alt={`${category.brand} ${category.model}`}
                    className="w-full h-full object-cover object-top hover:scale-105 transition-transform duration-300"
                />
            </div>
            <div className="p-4">
                <div className="mb-2">
                    <p className="text-sm text-gray-500 font-medium">
                        {category.brand}
                    </p>
                    <h3 className="text-lg font-semibold text-gray-900 line-clamp-2">
                        {category.model}
                    </h3>
                </div>
                <div className="flex items-center justify-between mb-2">
                    <div>
                        <p className="text-xs text-gray-500">
                            현재가
                        </p>
                        <p className="text-lg font-bold text-blue-600">
                            ₩{category.currentBid}
                        </p>
                    </div>
                    <div className="text-right">
                        <p className="text-xs text-gray-500">
                            참여자
                        </p>
                        <p className="text-sm font-semibold text-gray-900">
                            {category.bidders}명
                        </p>
                    </div>
                </div>
                <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-1">
                        <i className="fas fa-clock text-red-500 text-xs" />
                        <span className="text-sm font-medium text-red-500">
                            {category.timeLeft}
                        </span>
                    </div>
                    <button
                        onClick={() => { }}
                        className="px-3 py-1 bg-blue-500 text-white text-xs rounded-lg hover:bg-blue-600 cursor-pointer whitespace-nowrap">
                        입찰하기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CategoryItem;