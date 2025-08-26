import type { AuctionItem } from "./AuctionItem";

export interface AuctionItemProps {
    item: AuctionItem;
    onBidClick: (item: AuctionItem) => void;
}