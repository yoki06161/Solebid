import { useState } from 'react';
import Pagination from '../../../components/Pagination';
import { usePagination } from '../../../hooks/usePagination';
import { WishList, WishSearch } from '../components';
import { categories } from '../components/mockData';
import { useWishes } from "../hooks/useWishes.ts";
import type { Wish } from '../types/Wish';

const WishPage = () => {
    const [selectedCategory, setSelectedCategory] = useState("전체");
    const [sortBy, setSortBy] = useState("priceHigh");

    const { wishes, isLoading, error, removeWish } = useWishes();

    const filteredWishes = wishes?.filter(
        (item) => selectedCategory === "전체" || item.category === selectedCategory,
    ) || [];

    const sortedByCategory: { [key: string]: (a: Wish, b: Wish) => number; } = {
        priceHigh: (a, b) => (b.currentBid ?? 0) - (a.currentBid ?? 0),
        priceLow: (a, b) => (a.currentBid ?? 0) - (b.currentBid ?? 0),
    };

    const sortedItem = [...filteredWishes].sort(sortedByCategory[sortBy]);

    const {
        paginatedData: paginatedWishes,
        currentPage,
        setCurrentPage,
        totalPages
    } = usePagination({ data: sortedItem, itemsPerPage: 8 });

    const handleRemoveFromWishList = (id: number) => {
        removeWish(id);
    };

    if (isLoading) {
        return (
            <div className="fixed top-0 left-0 w-full h-full flex justify-center items-center">
                <i className="fas fa-spinner fa-spin fa-3x"></i>
            </div>
        );
    }

    if (error) {
        return <div>Error: {error.message}</div>;
    }

    return (
        <main className="min-h-screen bg-gray-50 relative">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                <WishSearch
                    categories={categories}
                    selectedCategory={selectedCategory}
                    onCategoryChange={setSelectedCategory}
                    sortBy={sortBy}
                    onSortChange={setSortBy}
                />
                <WishList
                    items={paginatedWishes}
                    onRemove={handleRemoveFromWishList}
                    onNavigateToBid={() => { }}
                />
                {filteredWishes.length > 0 && (
                    <Pagination
                        currentPage={currentPage}
                        totalPages={totalPages}
                        onPageChange={setCurrentPage}
                    />
                )}
            </div>
        </main>
    );
};

export default WishPage;
