import type { Product } from "./Brand";

export interface BrandModalProps {
    isOpen: boolean;
    onClose: () => void;
    product: Product | null;
    onSubmit: (bidAmount: number) => void;
}