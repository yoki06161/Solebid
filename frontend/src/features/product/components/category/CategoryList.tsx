import type { CategoryListProps } from "../../types/category/CategoryListProps";
import CategoryItem from "./CategoryItem";

const CategoryList: React.FC<CategoryListProps> = ({ categories }) => {
    return (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 mb-12">
            {categories.map((category) => (
                <CategoryItem
                    key={category.id}
                    category={category}
                />
            ))}
        </div>
    );
};

export default CategoryList;