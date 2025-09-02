export interface MainProduct {
    id: number;
    image: string;
    name: string;
    price: string;
    bidCount: number;
    timeLeft: string;
}

export interface MainProductProps {
    product: MainProduct
}