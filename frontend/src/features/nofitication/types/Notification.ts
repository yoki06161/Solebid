export interface Notification {
    id: number;
    image: string;
    title: string;
    isNew?: boolean;
    myBid?: boolean;
    myBidAmount: number;
    currentBid: number;
    timeLeft: string;
    bidders: number;
}