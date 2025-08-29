import { useState } from 'react';
import Pagination from '../../../components/Pagination';
import Toast from '../../../components/Toast';
import { usePagination } from '../../../hooks/usePagination';
import { WishList, WishSearch } from '../components';
import { categories, wishes } from '../components/mockData';
import type { Wish } from '../types/Wish';

const WishPage = () => {
    const [selectedCategory, setSelectedCategory] = useState("전체");
    const [sortBy, setSortBy] = useState("latest");
    const [wishItem, setWishItem] = useState(wishes);
    const [showToast, setShowToast] = useState(false);

    const filteredWishes = wishItem.filter(
        (item) => selectedCategory === "전체" || item.category === selectedCategory,
    );

    const sortedByCategory: { [key: string]: (a: Wish, b: Wish) => number; } = {
        priceHigh: (a, b) => b.price - a.price,
        priceLow: (a, b) => a.price - b.price,
        oldest: (a, b) => new Date(a.dateAdded).getTime() - new Date(b.dateAdded).getTime(),
        latest: (a, b) => new Date(b.dateAdded).getTime() - new Date(a.dateAdded).getTime(),
    };

    const sortedItem = [...filteredWishes].sort(sortedByCategory[sortBy] || sortedByCategory.latest);

    const {
        paginatedData: paginatedWishes,
        currentPage,
        setCurrentPage,
        totalPages
    } = usePagination({ data: sortedItem, itemsPerPage: 8 });

    const handleRemoveFromWishList = (id: number) => {
        setWishItem((prev) => prev.filter((item) => item.id !== id));
    };

    const handleAddToCart = () => {
        setShowToast(true);
        setTimeout(() => { setShowToast(false); }, 2500);
    };

    return (
        <main className="min-h-screen bg-gray-50 relative">
            {showToast && <Toast message='장바구니에 추가되었습니다' />}
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
                    onAddToCart={handleAddToCart}
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
