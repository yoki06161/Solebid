import { getStatusColor } from "../../../utils/get-status-color";
import type { TransactionItemProps } from "../types/TransactionItemProps";

const TransactionItem = ({ item }: TransactionItemProps) => {
    return (
        <div className="p-6 hover:bg-gray-50 transition-colors">
            <div className="flex items-center justify-between">
                <div className="flex items-center flex-1">
                    <img
                        src={item.image}
                        alt={item.name}
                        className="w-20 h-20 rounded-lg object-cover mr-4"
                    />
                    <div className="flex-1">
                        <h3 className="font-medium text-gray-900 mb-1">
                            {item.name}
                        </h3>
                        <p className="text-gray-600 text-sm mb-2">
                            판매일: {item.date}
                        </p>
                        <div className="flex items-center space-x-4">
                            <span className="font-semibold text-gray-900">
                                ₩{item.price.toLocaleString()}
                            </span>
                            <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(item.status)}`}>
                                {item.statusText}
                            </span>
                        </div>
                    </div>
                </div>
                <div className="flex items-center space-x-2">
                    <button
                        onClick={() => { }}
                        className="px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 !rounded-button whitespace-nowrap"
                    >
                        상세보기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default TransactionItem;