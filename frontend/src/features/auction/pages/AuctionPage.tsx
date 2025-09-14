import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { useWishes } from "../../wish/hooks/useWishes.ts";
import { AuctionList, AuctionModal, AuctionSearch } from "../components";
import { categories, sortOptions } from "../components/mockData";
import { getProducts } from "../services/AuctionService";
import type { AuctionItem } from "../types/AuctionItem";

const AuctionPage = () => {
    const { data: products, isLoading, isError, error } = useQuery({
        queryKey: ['products'],
        queryFn: () => getProducts(),
        select: (response) => response.data,
    });

    const { wishes, addWish, removeWish, isAdding, isRemoving } = useWishes();

    const wishedIds = useMemo(() => new Set(wishes?.map(wish => wish.id) ?? []), [wishes]);

    const [selectedCategory, setSelectedCategory] = useState('전체');
    const [priceRange, setPriceRange] = useState([0, 1000000]);
    const [sortOption, setSortOption] = useState('남은 시간순');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState<AuctionItem | null>(null);

    const processedItems = useMemo(() => {
        return (products ?? []).map(item => ({ ...item, isWished: wishedIds.has(item.id) }));
    }, [products, wishedIds]);

    const filteredItems = useMemo(() => {
        return processedItems.filter(item => {
            const categoryMatch = selectedCategory === '전체' || item.category === selectedCategory;
            const price = item.currentBid || 0;
            const priceMatch = price >= priceRange[0] && price <= priceRange[1];
            return categoryMatch && priceMatch;
        })
            .sort((a, b) => {
                // 정렬 로직 (필요에 따라 추가)
                if (sortOption === '인기순') {
                    return b.bidders - a.bidders;
                }
                // 기본은 '남은시간순' 또는 다른 옵션
                return (a.timeLeft || '').localeCompare(b.timeLeft || '');
            });
    }, [processedItems, selectedCategory, priceRange, sortOption]);

    const handleBidClick = (item: AuctionItem) => {
        setSelectedItem(item);
        setIsModalOpen(true);
    };

    // const handleBidSubmit = (bidAmount: number) => {
    //    if (selectedItem) {
    //        console.log(`Submitted bid: ₩${bidAmount.toLocaleString()} for ${selectedItem.name}`);
    //        // 실제 API 호출 로직 추가
    //        const updatedItems = products.map(item =>
    //            item.id === selectedItem.id
    //                ? { ...item, currentBid: bidAmount.toLocaleString(), bidders: item.bidders + 1 }
    //                : item
    //        );
    //        setProducts(updatedItems);
    //    }
    //};

    if (isLoading) {
        return (
            <div className="fixed top-0 left-0 w-full h-full flex justify-center items-center">
                <i className="fas fa-spinner fa-spin fa-3x"></i>
            </div>
        );
    }

    if (isError) {
        return (
            <div>Error: {error.message}</div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <main className="max-w-[1440px] mx-auto px-6 pt-6 pb-12">
                <AuctionSearch
                    categories={categories}
                    selectedCategory={selectedCategory}
                    onCategoryChange={setSelectedCategory}
                    sortOptions={sortOptions}
                    sortOption={sortOption}
                    onSortChange={setSortOption}
                    priceRange={priceRange}
                    onPriceChange={setPriceRange}
                />
                <AuctionList
                    items={filteredItems}
                    addWish={addWish}
                    removeWish={removeWish}
                    isAdding={isAdding}
                    isRemoving={isRemoving}
                    onBidClick={handleBidClick}
                />
            </main>
            {isModalOpen && selectedItem && (
                <AuctionModal
                    isOpen={isModalOpen}
                    onClose={() => {
                        setIsModalOpen(false);
                        setSelectedItem(null);
                    }}
                    currentBid={selectedItem.currentBid ?? 0}
                    onSubmit={() => { }}
                />
            )}
        </div>
    );
};

export default AuctionPage;