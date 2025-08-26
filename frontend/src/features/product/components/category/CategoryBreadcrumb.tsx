import type { CategoryBreadcrumbProps } from "../../types/category/CategoryBreadcrumb";

const CategoryBreadcrumb: React.FC<CategoryBreadcrumbProps> = ({ categoryName }) => {
    return (
        <div className="max-w-[1440px] mx-auto px-6 py-4">
            <div className="flex items-center space-x-2 text-sm text-gray-500">
                <a
                    href="/"
                    className="hover:text-gray-700 cursor-pointer">
                    홈
                </a>
                <i className="fas fa-chevron-right text-xs" />
                <span className="text-gray-900">
                    {categoryName}
                </span>
            </div>
        </div>
    );
};

export default CategoryBreadcrumb;