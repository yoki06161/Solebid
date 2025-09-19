import {apiFetch} from "../../../utils/apiFetch.ts";
import type {Order} from "../types/Order.ts";

export interface OrderCreatePayload {
    auctionId: number;
    deliveryAddress: string;
}

export const fetchCreateOrder = (payload: OrderCreatePayload): Promise<Order> =>
    apiFetch<Order>('/api/orders', {
        method: 'POST',
        json: payload,
    });

export const fetchWinningOrders = (): Promise<Order[]> =>
    apiFetch<Order[]>('/api/orders/winnings');

export const fetchOrderDetails = (orderId: number): Promise<Order> =>
    apiFetch<Order>(`/api/orders/${orderId}`);

