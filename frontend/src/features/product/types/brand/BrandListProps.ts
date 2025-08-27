import type { BrandWithProducts, Product } from "./Brand";

export interface BrandListProps {
    brandData: BrandWithProducts;
    onBidClick: (product: Product) => void;
}