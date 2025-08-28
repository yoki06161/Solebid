import type { Cart } from "./Cart";

export interface CartListProps {
    items: Cart[];
    isEditing: boolean;
    onToggleEdit: () => void;
    onRemoveItem: (id: number) => void;
}