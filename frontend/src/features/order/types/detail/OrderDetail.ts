import type { OrderDetailCustomer } from "./OrderDetailCustomerProps";
import type { OrderDetailListProps } from "./OrderDetailListProps";
import type { OrderDetailPayment } from "./OrderDetailPaymentProps";
import type { OrderDetailShipping } from "./OrderDetailShippingProps";
import type { OrderDetailTimeline } from "./OrderDetailTimelineProps";

export interface OrderDetail {
  id: string;
  date: string;
  status: string;
  statusColor: string;
  totalAmount: number;
  items: OrderDetailListProps[];
  payment: OrderDetailPayment;
  shipping: OrderDetailShipping;
  customer: OrderDetailCustomer;
  timeline: OrderDetailTimeline[];
}

export interface OrderDetailItem {
  image: string;
  name: string;
  options: string;
  quantity: number;
  price: number;
}