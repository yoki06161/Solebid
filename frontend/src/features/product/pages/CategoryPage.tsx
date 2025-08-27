import { useMemo, useState } from "react";
import { useParams } from 'react-router-dom';
import Pagination from "../../../components/Pagination";
import { CategoryBreadcrumb, CategoryHeader, CategoryList, CategorySearch } from "../components/category";
import { brands, categories, priceRanges, sortOptions } from "../components/category/mockData";
import type { Category } from "../types/category/Category";

const ITEMS_PER_PAGE = 4;

const CategoryPage = () => {
    const [selectedBrands, setSelectedBrands] = useState<string[]>([]);
    const [selectedPriceRange, setSelectedPriceRange] = useState<string>('');
    const [sortBy, setSortBy] = useState<string>('popular');
    const [currentPage, setCurrentPage] = useState<number>(1);

    const { categoryName } = useParams<{ categoryName: string }>();

    const decodedCategory = categoryName ? decodeURIComponent(categoryName) : '';

    const filteredAndSortedCategories = useMemo<Category[]>(() => {
        let result: Category[] = categories;

        if (selectedBrands.length > 0) {
            result = result.filter((sneaker) => selectedBrands.includes(sneaker.brand));
        }

        if (selectedPriceRange) {
            const [min, max] = selectedPriceRange.split('-').map(Number);
            result = result.filter(sneaker =>
                sneaker.price >= min && (max ? sneaker.price <= max : true)
            );
        }

        return [...result].sort((a, b) => {
            switch (sortBy) {
                case 'popular':
                    return b.bidders - a.bidders;
                case 'newest':
                    return new Date(b.releaseDate).getTime() - new Date(a.releaseDate).getTime();
                case 'price-asc':
                    return a.price - a.price;
                case 'price-desc':
                    return b.price - a.price;
                default:
                    return 0;
            }
        });
    }, [selectedBrands, selectedPriceRange, sortBy]);

    const handleBrandFilter = (brand: string) => {
        setSelectedBrands((prev) =>
            prev.includes(brand)
                ? prev.filter((b) => b !== brand)
                : [...prev, brand]
        );
        setCurrentPage(1);
    };

    const totalPages = Math.ceil(filteredAndSortedCategories.length / ITEMS_PER_PAGE);

    const paginatedCategories = filteredAndSortedCategories.slice(
        (currentPage - 1) * ITEMS_PER_PAGE,
        currentPage * ITEMS_PER_PAGE
    );

    return (
        <div className="min-h-screen bg-gray-50">
            <CategoryBreadcrumb
                categoryName={decodedCategory}
            />
            <div className="max-w-[1440px] mx-auto px-6 pb-12">
                <CategoryHeader
                    categoryName={decodedCategory}
                />
                <CategorySearch
                    brands={brands}
                    priceRanges={priceRanges}
                    sortOptions={sortOptions}
                    selectedBrands={selectedBrands}
                    handleBrandFilter={handleBrandFilter}
                    selectedPriceRange={selectedPriceRange}
                    setSelectedPriceRange={setSelectedPriceRange}
                    sortBy={sortBy}
                    setSortBy={setSortBy}
                />
                <div className="flex items-center justify-between mb-6">
                    <p className="text-gray-600">
                        총
                        <span className="font-semibold text-gray-900">
                            {filteredAndSortedCategories.length}
                        </span>
                        개의 상품
                    </p>
                    <div className="flex items-center space-x-2">
                        <button className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50 cursor-pointer">
                            <i className="fas fa-th text-gray-600" />
                        </button>
                        <button className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50 cursor-pointer">
                            <i className="fas fa-list text-gray-600" />
                        </button>
                    </div>
                </div>
                <CategoryList
                    categories={paginatedCategories}
                />
                <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={setCurrentPage}
                />
            </div>
        </div>
    );
}

export default CategoryPage;