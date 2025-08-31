export interface OrderDetailShipping {
  recipient: string;
  phone: string;
  zipCode: string;
  address: string;
  addressDetail: string;
  request: string;
  trackingNumber: string;
  courier: string;
}

export interface OrderDetailShippingProps {
  shipping: OrderDetailShipping;
}