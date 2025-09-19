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
                            <div>
                                <h3 className="text-lg font-semibold text-gray-900">
                                    {order.trackingNumber}
                                </h3>
                                <p className="text-gray-600 text-sm">
                                    {order.date || '-'}
                                </p>
                            </div>
                            <div className="flex items-center space-x-3">
                                <span className={getBadgeClass(order.statusColor || 'gray')}>
                                    {order.status || '-'}
                                </span>
                            </div>
                        </div>
                        <div className="flex items-center justify-between">
                            <div className="flex items-center space-x-4">
                                {order.items && order.items.length > 0 && (
                                    <>
                                        <img
                                            src={order.items[0].image || 'https://via.placeholder.com/64x64?text=No+Image'}
                                            alt={order.items[0].name || '-'}
                                            className="w-16 h-16 rounded-lg object-cover"
                                            onError={(e) => {
                                                const target = e.target as HTMLImageElement;
                                                target.src = 'https://via.placeholder.com/64x64?text=No+Image';
                                            }}
                                        />
                                        <div>
                                            <h4 className="font-medium text-gray-900">
                                                {order.items[0].name || '-'}
                                            </h4>
                                            {order.items.length > 1 && (
                                                <p className="text-gray-600 text-sm">
                                                    외 {order.items.length - 1}개 상품
                                                </p>
                                            )}
                                        </div>
                                    </>
                                )}
                            </div>
                            <div className="text-right">
                                <div className="text-xl font-bold text-gray-900">
                                    {order.finalPrice?.toLocaleString() || '0'}원
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