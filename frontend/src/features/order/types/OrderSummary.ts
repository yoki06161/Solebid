import type { Order } from "./Order";

export type OrderDetailSummaryProps = Pick<
    Order,
    "id" | "date" | "status" | "statusColor" | "totalAmount"
>;