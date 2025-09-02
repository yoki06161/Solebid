import { useNavigate } from "react-router-dom";
import type { MainCategoryListProps } from "../types/MainCategoryListProps";
import MainCategory from "./MainCategory";

const CategoryList = ({ categories }: MainCategoryListProps) => {
    const navigate = useNavigate();

    const handleCategoryClick = (categoryName: string) => {
        navigate(`/category/${categoryName}`);
    };

    return (
        <div className="max-w-[1440px] mx-auto px-6 py-12">
            <h3 className="text-xl font-semibold text-gray-900 mb-6">
                카테고리
            </h3>
            <div className="grid grid-cols-4 gap-4">
                {categories.map((category) => (
                    <MainCategory
                        key={category.name}
                        category={category}
                        onClick={() => handleCategoryClick(category.name)}
                    />
                ))}
            </div>
        </div>
    );
};

export default CategoryList;