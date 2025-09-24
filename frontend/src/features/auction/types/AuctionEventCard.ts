export interface AuctionEventCard {
    auctionEventId: number;
    productId: number;
    brand: string;
    name: string;
    category: string;
    imageUrl: string | null;
    currentBid: number;
    endAt: string; // ISO string
    bidders: number;
}
