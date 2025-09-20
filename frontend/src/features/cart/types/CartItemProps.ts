import type { CartItem } from "./CartItem";

export interface CartItemProps {
    item: CartItem;
    isEditing: boolean;
    onRemoveItem: (id: number) => void;
}