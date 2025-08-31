import { getFormatPrice } from "../../../utils/get-format-price";
import type { NotificationItemProps } from "../types/NotificationItemProps";

const NotificationItem = ({ notification, onNavigate }: NotificationItemProps) => {
    return (
        <div
            onClick={() => onNavigate(notification.id)}
            className="flex flex-col bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow duration-200 overflow-hidden cursor-pointer relative"
        >
            {notification.isNew && (
                <div className="bg-red-500 text-white text-xs px-2 py-1 absolute top-0 left-0 z-10 rounded-r-lg">
                    NEW
                </div>
            )}
            {notification.myBid && (
                <div className="bg-blue-500 text-white text-xs px-2 py-1 absolute top-6 left-0 z-10 rounded-r-lg">
                    참여중
                </div>
            )}
            <div className="relative h-48 overflow-hidden">
                <img
                    src={notification.image}
                    alt={notification.title}
                    className="w-full h-full object-cover object-top"
                />
            </div>
            <div className="flex flex-col flex-grow p-4">
                <h3 className="font-semibold text-gray-900 mb-2 line-clamp-2 h-12">
                    {notification.title}
                </h3>
                <div className="space-y-2 mb-4">
                    <div className="flex justify-between items-center">
                        <span className="text-sm text-gray-500">
                            현재 최고가
                        </span>
                        <span className="text-lg font-bold text-blue-600">{
                            getFormatPrice(notification.currentBid)}
                        </span>
                    </div>
                    {notification.myBid && (
                        <div className="flex justify-between items-center">
                            <span className="text-sm text-gray-500">내 입찰가</span>
                            <span className={
                                `text-sm font-medium
                                 ${notification.currentBid > notification.myBidAmount
                                    ? "text-red-600"
                                    : "text-green-600"
                                }`
                            }>
                                {getFormatPrice(notification.myBidAmount)}
                            </span>
                        </div>
                    )}
                    <div className="flex justify-between items-center">
                        <span className="text-sm text-gray-500">
                            남은 시간
                        </span>
                        <span className="text-sm font-medium text-red-600">
                            {notification.timeLeft}
                        </span>
                    </div>
                    <div className="flex justify-between items-center">
                        <span className="text-sm text-gray-500">
                            입찰자 수
                        </span>
                        <span className="text-sm font-medium text-gray-900">
                            {notification.bidders}명
                        </span>
                    </div>
                </div>
                <button
                    onClick={(e) => {
                        e.stopPropagation();
                        console.log(`Bid on item ${notification.id}`);
                    }}
                    className="w-full mt-auto bg-blue-600 text-white py-2 px-4 rounded-md text-sm font-medium hover:bg-blue-700 transition-colors duration-200 !rounded-button whitespace-nowrap"
                >
                    즉시 입찰
                </button>
            </div>
        </div>
    );
};

export default NotificationItem;