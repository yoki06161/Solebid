import type { OrderListProps } from "../types/OrderList";
import OrderItem from "./OrderItem";

const OrderList = ({
    orders,
}: Omit<OrderListProps, 'expandedOrder' | 'toggleOrderExpansion'>) => {
    return (
        <div className="space-y-4">
            {orders.map((order) => (
                <OrderItem
                    key={order.id}
                    order={order}
                />
            ))}
        </div>
    );
};

export default OrderList;