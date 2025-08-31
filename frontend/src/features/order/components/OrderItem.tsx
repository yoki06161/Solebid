import { Link } from "react-router-dom";
import { getBadgeClass } from "../../../utils/get-badge-class";
import type { OrderItemProps } from "../types/OrderItemProps";

const OrderItem = ({ order, isExpanded, onToggleExpand }: OrderItemProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
            <div
                onClick={() => onToggleExpand(order.id)}
                className="p-6 cursor-pointer hover:bg-gray-50"   >
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
                                <i className={`fas fa-chevron-${isExpanded ? "up" : "down"} text-gray-400`} />
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
                                {(order.status === "배송중" ||
                                    order.status === "배송완료") &&
                                    order.trackingNumber && (
                                        <button className="mt-2 px-3 py-1 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 cursor-pointer !rounded-button whitespace-nowrap">
                                            배송 추적
                                        </button>
                                    )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            {/* Expanded Order Details */}
            {isExpanded && (
                <div className="border-t border-gray-200 bg-gray-50 p-6">
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        <div>
                            <h4 className="font-semibold text-gray-900 mb-3">
                                주문 상품
                            </h4>
                            <div className="space-y-3">
                                {order.items.map((item, index) => (
                                    <div
                                        key={index}
                                        className="flex items-center space-x-3 bg-white p-3 rounded-lg"
                                    >
                                        <img
                                            src={item.image}
                                            alt={item.name}
                                            className="w-12 h-12 rounded-lg object-cover"
                                        />
                                        <div className="flex-1">
                                            <h5 className="font-medium text-gray-900">
                                                {item.name}
                                            </h5>
                                            <p className="text-gray-600 text-sm">
                                                수량: {item.quantity}개
                                            </p>
                                        </div>
                                        <div className="text-right">
                                            <div className="font-semibold text-gray-900">
                                                {(item.price * item.quantity).toLocaleString()}
                                                원
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                        {/* Order Details Info */}
                        <div>
                            <h4 className="font-semibold text-gray-900 mb-3">
                                주문 정보
                            </h4>
                            <div className="bg-white p-4 rounded-lg space-y-3">
                                <div className="flex justify-between">
                                    <span className="text-gray-600">주문번호</span>
                                    <span className="font-medium">{order.id}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-600">주문일자</span>
                                    <span className="font-medium">{order.date}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-600">결제금액</span>
                                    <span className="font-semibold text-lg">
                                        {order.totalAmount.toLocaleString()}원
                                    </span>
                                </div>
                                {order.trackingNumber && (
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">운송장번호</span>
                                        <span className="font-medium">
                                            {order.trackingNumber}
                                        </span>
                                    </div>
                                )}
                                <div className="pt-2 border-t border-gray-200">
                                    <span className="text-gray-600 text-sm">배송지</span>
                                    <p className="font-medium text-sm mt-1">
                                        {order.deliveryAddress}
                                    </p>
                                </div>
                            </div>
                            {/* Order Details Button */}
                            <div className="mt-4 flex space-x-2">
                                <Link
                                    // to={`/order:${order.id}`}
                                    to="/order/detail"
                                    className="flex-1 w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 cursor-pointer !rounded-button whitespace-nowrap text-center"
                                >
                                    주문 상세
                                </Link>
                                {(
                                    order.status === "결제완료" ||
                                    order.status === "배송준비중") && (
                                        <button className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 cursor-pointer !rounded-button whitespace-nowrap">
                                            주문 취소
                                        </button>
                                    )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default OrderItem;