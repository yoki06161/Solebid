export interface OrderShipping {
    recipient: string;
    phone: string;
    address: string;
    addressDetail: string;
    zipCode: string;
    request: string;
    trackingNumber: string;
    courier: string;
}

export interface OrderDetailShippingProps {
    shipping: OrderShipping;
}