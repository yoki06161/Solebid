import type { AuctionItemProps } from "../../types/auction/AuctionItemProps";

const AuctionItem: React.FC<AuctionItemProps> = ({ item, onBidClick }) => {
    return (
        <div className="bg-white rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow">
            <div className="relative h-64">
                <img
                    src={item.image}
                    alt={item.name}
                    className="w-full h-full object-cover"
                />
            </div>
            <div className="p-6">
                <div className="text-sm text-gray-500 mb-1">
                    {item.brand}
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-4">
                    {item.name}
                </h3>
                <div className="flex justify-between items-center mb-2">
                    <span className="text-sm text-gray-600">
                        현재 입찰가
                    </span>
                    <span className="text-lg font-semibold text-blue-600">
                        ₩{item.currentBid}
                    </span>
                </div>
                <div className="flex justify-between items-center mb-4">
                    <span className="text-sm text-gray-600">
                        남은 시간
                    </span>
                    <span className="text-sm font-medium text-red-500">
                        {item.timeLeft}
                    </span>
                </div>
                <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-500">
                        {item.bidders}명 참여
                    </span>
                    <button
                        onClick={() => onBidClick(item)}
                        className="px-6 py-2 bg-blue-500 text-white !rounded-button hover:bg-blue-600 whitespace-nowrap cursor-pointer"
                    >
                        입찰하기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AuctionItem;