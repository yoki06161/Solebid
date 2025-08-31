import { getFormatPrice } from "../../../utils/get-format-price";
import type { NotificationPushProps } from "../types/NotificationPushProps";

const NotificationPush = ({ notification, onClose }: NotificationPushProps) => {
    return (
        <div className="fixed top-4 right-4 bg-white border border-red-200 rounded-lg shadow-lg p-4 z-50 max-w-sm animate-slide-in">
            <div className="flex items-start space-x-3">
                <div className="flex-shrink-0">
                    <i className="fas fa-exclamation-triangle text-red-500 text-lg"></i>
                </div>
                <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900">
                        입찰가 갱신 알림
                    </p>
                    <p className="text-sm text-gray-600 mt-1 truncate">
                        {notification.title}
                    </p>
                    <div className="mt-2">
                        <p className="text-xs text-gray-500">
                            현재 최고가:{" "}
                            <span className="font-semibold text-red-600">
                                {getFormatPrice(notification.currentBid)}
                            </span>
                        </p>
                        <p className="text-xs text-gray-500">
                            내 입찰가:{" "}
                            <span className="font-semibold">
                                {getFormatPrice(notification.myBidAmount)}
                            </span>
                        </p>
                    </div>
                </div>
                <button
                    onClick={onClose}
                    className="flex-shrink-0 text-gray-400 hover:text-gray-600 cursor-pointer"
                >
                    <i className="fas fa-times text-sm"></i>
                </button>
            </div>
        </div>
    );
}

export default NotificationPush;