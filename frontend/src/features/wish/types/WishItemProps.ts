import type { Wish } from "./Wish";

export interface WishItemProps {
    item: Wish;
    onRemove: (id: number) => void;
}