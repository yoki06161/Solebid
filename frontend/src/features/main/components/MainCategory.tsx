import type { MainCategoryProps } from "../types/MainCategoryProps";

const MainCategory = ({ category, onClick }: MainCategoryProps) => {
    return (
        <div
            className="bg-white p-6 rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer"
            onClick={onClick}
        >
            <div className="flex items-center justify-between">
                <div>
                    <h4 className="text-lg font-medium text-gray-900">
                        {category.name}
                    </h4>
                    <p className="text-sm text-gray-500 mt-1">
                        {category.count}개의 상품
                    </p>
                </div>
                <i className={`fas ${category.icon} text-2xl text-blue-500`} />
            </div>
        </div>
    );
};

export default MainCategory;