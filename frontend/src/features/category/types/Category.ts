export interface Category {
    id: number;
    brand: string;
    model: string;
    image: string;
    currentBid: number;
    bidders: number;
    timeLeft: string;
    price: number;
    releaseDate: string; // "YYYY-MM-DD"
}