export interface AuctionItem {
    id: number;
    brand: string;
    name: string;
    image: string | null;
    currentBid: number | null;
    timeLeft: string;
    bidders: number;
    category: string;
}