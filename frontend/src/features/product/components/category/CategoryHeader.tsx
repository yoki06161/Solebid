import type { CategoryHeaderProps } from "../../types/category/CategoryHeaderProps";

const CategoryHeader: React.FC<CategoryHeaderProps> = ({ categoryName }) => {
    return (
        <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
                {categoryName} 경매
            </h1>
            <p className="text-gray-600">
                다양한 브랜드의 프리미엄 {categoryName}를 경매로 만나보세요
            </p>
        </div>
    );
};

export default CategoryHeader;