import React, { useState } from "react";

interface BidModalProps {
    isOpen: boolean;
    onClose: () => void;
    currentBid: string;
    onSubmit: (bidAmount: number) => void;
}

interface selectedType {
    id: number,
    brand: string,
    name: string,
    image: string,
    currentBid: string,
    timeLeft: string,
    bidders: number,
    category: string
}

const BidModal: React.FC<BidModalProps> = ({
    isOpen,
    onClose,
    currentBid,
    onSubmit,
}) => {
    const [bidAmount, setBidAmount] = useState<string>("");

    const minBidIncrement = 10000;
    const currentBidNumber = parseInt(currentBid.replace(/,/g, ""));
    const minBidAmount = currentBidNumber + minBidIncrement;

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const amount = parseInt(bidAmount);
        if (amount >= minBidAmount) {
            onSubmit(amount);
            onClose();
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
                <h3 className="text-xl font-semibold mb-4">입찰하기</h3>
                <div className="mb-4">
                    <p className="text-gray-600">현재 입찰가: ₩{currentBid}</p>
                    <p className="text-gray-600">
                        최소 입찰 단위: ₩{minBidIncrement.toLocaleString()}
                    </p>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-700 mb-2">입찰 금액</label>
                        <input
                            type="number"
                            min={minBidAmount}
                            step={minBidIncrement}
                            value={bidAmount}
                            onChange={(e) => setBidAmount(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder={`최소 입찰가: ₩${minBidAmount.toLocaleString()}`}
                            required
                        />
                    </div>
                    <div className="flex space-x-4">
                        <button
                            type="submit"
                            className="flex-1 px-4 py-2 bg-blue-500 text-white !rounded-button hover:bg-blue-600 whitespace-nowrap"
                        >
                            입찰하기
                        </button>
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 !rounded-button hover:bg-gray-50 whitespace-nowrap"
                        >
                            취소
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

const Auction: React.FC = () => {
    const [selectedCategory, setSelectedCategory] = useState("전체");
    const [priceRange, setPriceRange] = useState([0, 1000000]);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [sortOption, setSortOption] = useState("남은시간순");
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);

    const categories = ["전체", "운동화", "구두", "샌들", "슬리퍼", "부츠"];
    const sortOptions = ["남은시간순", "인기순", "최신순"];
    const auctionItems = [
        {
            id: 1,
            brand: "나이키",
            name: "에어맥스 97 트리플 화이트",
            image:
                "https://readdy.ai/api/search-image?query=nike%20air%20max%2097%20triple%20white%20sneaker%20on%20minimal%20light%20gray%20background%20professional%20product%20photography%20with%20soft%20lighting%20high%20end%20commercial%20shot%20clean%20composition&width=400&height=300&seq=1&orientation=landscape",
            currentBid: "450,000",
            timeLeft: "2시간 32분",
            bidders: 23,
            category: "운동화",
        },
        {
            id: 2,
            brand: "아디다스",
            name: "울트라부스트 22",
            image:
                "https://readdy.ai/api/search-image?query=adidas%20ultraboost%2022%20sneaker%20on%20minimal%20light%20gray%20background%20professional%20product%20photography%20with%20soft%20lighting%20high%20end%20commercial%20shot%20clean%20composition&width=400&height=300&seq=2&orientation=landscape",
            currentBid: "380,000",
            timeLeft: "4시간 15분",
            bidders: 18,
            category: "운동화",
        },
        {
            id: 3,
            brand: "뉴발란스",
            name: "993 그레이",
            image:
                "https://readdy.ai/api/search-image?query=new%20balance%20993%20gray%20sneaker%20on%20minimal%20light%20gray%20background%20professional%20product%20photography%20with%20soft%20lighting%20high%20end%20commercial%20shot%20clean%20composition&width=400&height=300&seq=3&orientation=landscape",
            currentBid: "290,000",
            timeLeft: "1시간 45분",
            bidders: 31,
            category: "운동화",
        },
        {
            id: 4,
            brand: "구찌",
            name: "클래식 레더 로퍼",
            image:
                "https://readdy.ai/api/search-image?query=gucci%20classic%20leather%20loafer%20on%20minimal%20light%20gray%20background%20professional%20product%20photography%20with%20soft%20lighting%20high%20end%20commercial%20shot%20clean%20composition&width=400&height=300&seq=4&orientation=landscape",
            currentBid: "890,000",
            timeLeft: "5시간 20분",
            bidders: 15,
            category: "구두",
        },
        {
            id: 5,
            brand: "발렌시아가",
            name: "트랙 스니커즈",
            image:
                "https://readdy.ai/api/search-image?query=balenciaga%20track%20sneaker%20on%20minimal%20light%20gray%20background%20professional%20product%20photography%20with%20soft%20lighting%20high%20end%20commercial%20shot%20clean%20composition&width=400&height=300&seq=5&orientation=landscape",
            currentBid: "750,000",
            timeLeft: "3시간 10분",
            bidders: 27,
            category: "운동화",
        },
        {
            id: 6,
            brand: "버켄스탁",
            name: "아리조나 샌들",
            image:
                "https://readdy.ai/api/search-image?query=birkenstock%20arizona%20sandal%20on%20minimal%20light%20gray%20background%20professional%20product%20photography%20with%20soft%20lighting%20high%20end%20commercial%20shot%20clean%20composition&width=400&height=300&seq=6&orientation=landscape",
            currentBid: "180,000",
            timeLeft: "6시간 45분",
            bidders: 12,
            category: "샌들",
        },
    ];


    const [selectedItem, setSelectedItem] = useState<selectedType | null>(null);

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-[1440px] mx-auto px-6 pt-24 pb-12">
                <div className="bg-white shadow-sm rounded-lg p-6 mb-8">
                    <div className="flex flex-wrap items-center justify-between gap-4">
                        <div className="flex space-x-2">
                            {categories.map((category) => (
                                <button
                                    key={category}
                                    onClick={() => setSelectedCategory(category)}
                                    className={`px-4 py-2 !rounded-button whitespace-nowrap cursor-pointer ${selectedCategory === category
                                        ? "bg-blue-500 text-white"
                                        : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                                        }`}
                                >
                                    {category}
                                </button>
                            ))}
                        </div>
                        <div className="flex items-center space-x-4">
                            <div className="relative">
                                <button
                                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                                    className="px-4 py-2 bg-white border border-gray-300 !rounded-button text-gray-700 flex items-center space-x-2 cursor-pointer"
                                >
                                    <span>{sortOption}</span>
                                    <i className="fas fa-chevron-down text-sm"></i>
                                </button>
                                {isDropdownOpen && (
                                    <div className="absolute right-0 mt-2 w-40 bg-white border border-gray-200 rounded-lg shadow-lg z-20">
                                        {sortOptions.map((option) => (
                                            <button
                                                key={option}
                                                onClick={() => {
                                                    setSortOption(option);
                                                    setIsDropdownOpen(false);
                                                }}
                                                className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100 cursor-pointer"
                                            >
                                                {option}
                                            </button>
                                        ))}
                                    </div>
                                )}
                            </div>
                            <div className="w-64">
                                <input
                                    type="range"
                                    min="0"
                                    max="1000000"
                                    step="10000"
                                    value={priceRange[1]}
                                    onChange={(e) => setPriceRange([0, parseInt(e.target.value)])}
                                    className="w-full"
                                />
                                <div className="text-sm text-gray-600 mt-1">
                                    최대 가격: ₩{priceRange[1].toLocaleString()}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {auctionItems.map((item) => (
                        <div
                            key={item.id}
                            className="bg-white rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow"
                        >
                            <div className="relative h-64">
                                <img
                                    src={item.image}
                                    alt={item.name}
                                    className="w-full h-full object-cover"
                                />
                            </div>
                            <div className="p-6">
                                <div className="text-sm text-gray-500 mb-1">{item.brand}</div>
                                <h3 className="text-lg font-medium text-gray-900 mb-4">
                                    {item.name}
                                </h3>
                                <div className="flex justify-between items-center mb-2">
                                    <span className="text-sm text-gray-600">현재 입찰가</span>
                                    <span className="text-lg font-semibold text-blue-600">
                                        ₩{item.currentBid}
                                    </span>
                                </div>
                                <div className="flex justify-between items-center mb-4">
                                    <span className="text-sm text-gray-600">남은 시간</span>
                                    <span className="text-sm font-medium text-red-500">
                                        {item.timeLeft}
                                    </span>
                                </div>
                                <div className="flex items-center justify-between">
                                    <span className="text-sm text-gray-500">
                                        {item.bidders}명 참여
                                    </span>
                                    <button
                                        onClick={() => {
                                            setSelectedItem(item);
                                            setIsModalOpen(true);
                                        }}
                                        className="px-6 py-2 bg-blue-500 text-white !rounded-button hover:bg-blue-600 whitespace-nowrap cursor-pointer"
                                    >
                                        입찰하기
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            {/* Bid Modal */}
            {isModalOpen && selectedItem && (
                <BidModal
                    isOpen={isModalOpen}
                    onClose={() => {
                        setIsModalOpen(false);
                        setSelectedItem(null);
                    }}
                    currentBid={selectedItem.currentBid}
                    onSubmit={(bidAmount) => {
                        // Handle bid submission
                        console.log(
                            `Bid submitted: ₩${bidAmount.toLocaleString()} for ${selectedItem.name}`,
                        );
                        // Here you would typically make an API call to submit the bid
                    }}
                />
            )}
        </div>
    );
};

export default Auction;
