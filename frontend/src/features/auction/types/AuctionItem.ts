export interface AuctionItem {
    id: number;
    brand: string;
    name: string;
    image: string;
    currentBid: number;
    timeLeft: string;
    bidders: number;
    category: string;
}