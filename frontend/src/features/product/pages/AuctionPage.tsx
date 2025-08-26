import { useMemo, useState } from "react";
import { AuctionHeader, AuctionList, AuctionModal, AuctionSearch } from "../components/auction";
import { auctions, categories, sortOptions } from "../components/auction/mockData";
import type { AuctionItem } from "../types/auction/AuctionItem";

const AuctionPage = () => {
    const [auctionItems, setAuctionItems] = useState<AuctionItem[]>(auctions);
    const [selectedCategory, setSelectedCategory] = useState('전체');
    const [priceRange, setPriceRange] = useState([0, 1000000]);
    const [sortOption, setSortOption] = useState('남은 시간순');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState<AuctionItem | null>(null);

    const filteredItems = useMemo(() => {
        return auctionItems
            .filter(item => {
                const categoryMatch = selectedCategory === '전체' || item.category === selectedCategory;
                const price = parseInt(item.currentBid.replace(/,/g, ''), 10);
                const priceMatch = price >= priceRange[0] && price <= priceRange[1];
                return categoryMatch && priceMatch;
            })
            .sort((a, b) => {
                // 정렬 로직 (필요에 따라 추가)
                if (sortOption === '인기순') {
                    return b.bidders - a.bidders;
                }
                // 기본은 '남은시간순' 또는 다른 옵션
                return a.timeLeft.localeCompare(b.timeLeft);
            });
    }, [auctionItems, selectedCategory, priceRange, sortOption]);

    const handleBidClick = (item: AuctionItem) => {
        setSelectedItem(item);
        setIsModalOpen(true);
    };

    const handleBidSubmit = (bidAmount: number) => {
        if (selectedItem) {
            console.log(`Submitted bid: ₩${bidAmount.toLocaleString()} for ${selectedItem.name}`);
            // 실제 API 호출 로직 추가
            const updatedItems = auctionItems.map(item =>
                item.id === selectedItem.id
                    ? { ...item, currentBid: bidAmount.toLocaleString(), bidders: item.bidders + 1 }
                    : item
            );
            setAuctionItems(updatedItems);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <nav className="bg-white shadow-sm fixed w-full z-10">
                <AuctionHeader />
            </nav>
            <main className="max-w-[1440px] mx-auto px-6 pt-24 pb-12">
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
                    currentBid={selectedItem.currentBid}
                    onSubmit={handleBidSubmit}
                />
            )}
        </div>
    );
};

export default AuctionPage;