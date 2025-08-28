import { useState } from 'react';
import Toast from '../../../components/Toast';
import { WishList, WishSearch } from '../components';
import { categories, wishes } from '../components/mockData';
import type { Wish } from '../types/Wish';

const WishPage = () => {
    const [selectedCategory, setSelectedCategory] = useState("전체");
    const [sortBy, setSortBy] = useState("latest");
    const [wishItem, setWishItem] = useState(wishes);
    const [showToast, setShowToast] = useState(false);

    const handleRemoveFromWishList = (id: number) => {
        setWishItem((prev) => prev.filter((item) => item.id !== id));
    };

    const handleAddToCart = () => {
        setShowToast(true);
        setTimeout(() => {
            setShowToast(false);
        }, 2500);
    };

    const filteredItems = wishItem.filter(
        (item) => selectedCategory === "전체" || item.category === selectedCategory,
    );

    const sortedByCategory: { [key: string]: (a: Wish, b: Wish) => number; } = {
        priceHigh: (a, b) => b.price - a.price,
        priceLow: (a, b) => a.price - b.price,
        oldest: (a, b) => new Date(a.dateAdded).getTime() - new Date(b.dateAdded).getTime(),
        latest: (a, b) => new Date(b.dateAdded).getTime() - new Date(a.dateAdded).getTime(),
    };

    const sortedItem = [...filteredItems].sort(sortedByCategory[sortBy] || sortedByCategory.latest)

    return (
        <div className="min-h-screen bg-gray-50 relative">
            {showToast && <Toast message='장바구니에 추가되었습니다' />}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                <WishSearch
                    categories={categories}
                    selectedCategory={selectedCategory}
                    onCategoryChange={setSelectedCategory}
                    sortBy={sortBy}
                    onSortChange={setSortBy}
                />
                <WishList
                    items={sortedItem}
                    onRemove={handleRemoveFromWishList}
                    onAddToCart={handleAddToCart}
                />
            </main>
        </div>
    );
};

export default WishPage;
