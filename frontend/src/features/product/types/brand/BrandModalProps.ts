import type { BrandProduct } from "./Brand";

export interface BrandModalProps {
    isOpen: boolean;
    onClose: () => void;
    product: BrandProduct | null;
    onSubmit: (productId: number, bidAmount: number) => void;
}