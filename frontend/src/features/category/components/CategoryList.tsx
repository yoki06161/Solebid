import type { CategoryListProps } from "../types/CategoryListProps";
import CategoryItem from "./CategoryItem";

const CategoryList = ({ categories }: CategoryListProps) => {
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