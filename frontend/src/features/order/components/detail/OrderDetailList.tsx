import type { OrderDetailListProps } from "../../types/detail/OrderDetailListProps";

const OrderDetailList = ({ items }: OrderDetailListProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                주문 상품
            </h3>
            <div className="space-y-4">
                {items.map((item, index) => (
                    <div
                        key={index}
                        className="flex items-center space-x-4 p-4 border border-gray-200 rounded-lg"
                    >
                        <img
                            src={item.image}
                            alt={item.name}
                            className="w-20 h-20 rounded-lg object-cover"
                        />
                        <div className="flex-1">
                            <h4 className="font-medium text-gray-900 mb-1">
                                {item.name}
                            </h4>
                            <p className="text-gray-600 text-sm mb-1">
                                {item.options}
                            </p>
                            <p className="text-gray-600 text-sm">
                                수량: {item.quantity}개
                            </p>
                        </div>
                        <div className="text-right">
                            <div className="font-semibold text-gray-900">
                                {item.price.toLocaleString()}원
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default OrderDetailList;