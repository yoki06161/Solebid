import type { AuctionItem } from "./AuctionItem";

export interface AuctionListProps {
    items: AuctionItem[];
    onBidClick: (item: AuctionItem) => void;
}