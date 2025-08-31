import type { OrderDetail } from "./OrderDetail";

export type OrderDetailSummaryProps = Pick<
    OrderDetail,
    "id" | "date" | "status" | "statusColor" | "totalAmount"
>;