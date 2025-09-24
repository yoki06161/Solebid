import type { Wish } from "../../wish/types/Wish";
import type { AuctionItem } from "./AuctionItem";

export interface AuctionListProps {
    items: AuctionItem[];
    onBidClick: (item: AuctionItem) => void;
    onSelect: (item: AuctionItem) => void;
    addWish: (item: Wish) => void;
    removeWish: (id: number) => void;
    isAdding: boolean;
    isRemoving: boolean;
}
