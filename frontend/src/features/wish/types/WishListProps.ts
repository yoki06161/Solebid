import type { Wish } from "./Wish";

export interface WishListProps {
    items: Wish[];
    onRemove: (id: number) => void;
    onNavigateToBid: (id: number) => void;
}