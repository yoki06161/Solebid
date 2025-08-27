export interface Category {
    id: number;
    brand: string;
    model: string;
    image: string;
    currentBid: string;
    bidders: number;
    timeLeft: string;
    price: number;
    releaseDate: string; // "YYYY-MM-DD"
}