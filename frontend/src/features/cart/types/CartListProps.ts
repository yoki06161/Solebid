import type { CartItem } from "./CartItem";

export interface CartListProps {
    items: CartItem[];
    isEditing: boolean;
    onToggleEdit: () => void;
    onRemoveItem: (id: number) => void;
}