import type { OrderItem } from "./OrderItem";
import type { OrderPayment } from "./OrderPayment";
import type { OrderShipping } from "./OrderShipping";
import type { OrderTimeline } from "./OrderTimeline";

export interface Order {
    id: string;
    date: string;
    items: OrderItem[];
    totalAmount: number;
    status: string;
    statusColor: string;
    trackingNumber: string;
    deliveryAddress: string;
    payment: OrderPayment;
    shipping: OrderShipping;
    timeline: OrderTimeline[];
}

export interface OrderProps {
    order: Order;
}