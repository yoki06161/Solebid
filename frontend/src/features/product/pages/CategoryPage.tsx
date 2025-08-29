import { useMemo, useState } from "react";
import { useParams } from 'react-router-dom';
import Pagination from "../../../components/Pagination";
import { usePagination } from "../../../hooks/usePagination";
import { CategoryBreadcrumb, CategoryHeader, CategoryList, CategorySearch } from "../components/category";
import { brands, categories, priceRanges, sortOptions } from "../components/category/mockData";
import type { Category } from "../types/category/Category";

const CategoryPage = () => {
    const [selectedBrands, setSelectedBrands] = useState<string[]>([]);
    const [selectedPriceRange, setSelectedPriceRange] = useState<string>('');
    const [sortBy, setSortBy] = useState<string>('popular');

    const filteredCategories = useMemo<Category[]>(() => {
        const sortFns: Record<string, (a: Category, b: Category) => number> = {
            'popular': (a, b) => b.bidders - a.bidders,
            'newest': (a, b) => new Date(b.releaseDate).getTime() - new Date(a.releaseDate).getTime(),
            'price-asc': (a, b) => a.price - b.price,
            'price-desc': (a, b) => b.price - a.price,
        };

        const processedCategories = categories
            .filter(sneaker =>
                !selectedBrands.length || selectedBrands.includes(sneaker.brand)
            )
            .filter(sneaker => {
                if (!selectedPriceRange) return true;
                const [min, max] = selectedPriceRange.split('-').map(Number);
                return sneaker.price >= min && (isNaN(max) || sneaker.price <= max);
            });

        const sortFn = sortFns[sortBy] || (() => 0);

        return [...processedCategories].sort(sortFn);
    }, [selectedBrands, selectedPriceRange, sortBy]);

    const {
        paginatedData: paginatedCategories,
        currentPage,
        setCurrentPage,
        totalPages
    } = usePagination({ data: filteredCategories, itemsPerPage: 4 });

    const { categoryName } = useParams<{ categoryName: string }>();

    const decodedCategory = categoryName ? decodeURIComponent(categoryName) : '';

    const handleBrandFilter = (brand: string) => {
        setSelectedBrands((prev) =>
            prev.includes(brand)
                ? prev.filter((b) => b !== brand)
                : [...prev, brand]
        );
        setCurrentPage(1);
    };

    return (
        <main className="min-h-screen bg-gray-50">
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
                            {filteredCategories.length}
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
                {filteredCategories.length > 0 && (
                    <Pagination
                        currentPage={currentPage}
                        totalPages={totalPages}
                        onPageChange={setCurrentPage}
                    />
                )}
            </div>
        </main>
    );
}

export default CategoryPage;