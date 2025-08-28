import type { Bid } from "./Bid";

export interface BidFormProps {
    bidInfo: Bid;
    errors: { [key: string]: string };
    onInfoChange: (field: keyof Bid, value: string) => void;
    brands: string[];
    categories: string[];
    sizes: number[];
}