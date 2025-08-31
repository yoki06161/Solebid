import { Link } from "react-router-dom";
import { getBadgeClass } from "../../../utils/get-badge-class";
import type { OrderProps } from "../types/Order";

const OrderItem = ({ order }: Omit<OrderProps, 'isExpanded' | 'onToggleExpand'>) => {
    return (
        <Link
            to={`/order/${order.id}`}
            className="block bg-white rounded-lg shadow-sm border border-gray-200 hover:bg-gray-50"
        >
            <div className="p-6">
                <div className="flex justify-between items-start">
                    <div className="flex-1">
                        <div className="flex items-center justify-between mb-3">
                            <h3 className="text-lg font-semibold text-gray-900">
                                {order.id}
                            </h3>
                            <div className="flex items-center space-x-3">
                                <span className={getBadgeClass(order.statusColor)}>
                                    {order.status}
                                </span>
                            </div>
                        </div>
                        <div className="flex items-center justify-between">
                            <div className="flex items-center space-x-4">
                                <img
                                    src={order.items[0].image}
                                    alt={order.items[0].name}
                                    className="w-16 h-16 rounded-lg object-cover"
                                />
                                <div>
                                    <h4 className="font-medium text-gray-900">
                                        {order.items[0].name}
                                    </h4>
                                    {order.items.length > 1 && (
                                        <p className="text-gray-600 text-sm">
                                            외 {order.items.length - 1}개 상품
                                        </p>
                                    )}
                                </div>
                            </div>
                            <div className="text-right">
                                <div className="text-xl font-bold text-gray-900">
                                    {order.totalAmount.toLocaleString()}원
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Link>
    );
};

export default OrderItem;