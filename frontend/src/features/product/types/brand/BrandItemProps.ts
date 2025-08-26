import type { Product } from "./Brand";

export interface BrandItemProps {
    product: Product;
    onBidClick: (product: Product) => void;
}