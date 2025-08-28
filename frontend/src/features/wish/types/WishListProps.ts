import type { Wish } from "./Wish";

export interface WishListProps {
    items: Wish[];
    onRemove: (id: number) => void;
    onAddToCart: (id: number) => void;
}