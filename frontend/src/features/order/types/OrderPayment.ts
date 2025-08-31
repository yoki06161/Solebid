export interface OrderPayment {
    method: string;
    cardInfo?: string;
    status: string;
    itemAmount: number;
    shippingFee: number;
    discount: number;
    finalAmount: number;
}

export interface OrderDetailPaymentProps {
    payment: OrderPayment;
}