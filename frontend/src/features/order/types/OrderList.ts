import type { Order } from "./Order";
import type { OrderItem } from "./OrderItem";

export interface OrderListProps {
    orders: Order[];
}

export interface OrderDetailListProps {
    items: OrderItem[];
}