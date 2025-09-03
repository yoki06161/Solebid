export interface SearchProduct {
    id: number;
    name: string;
    brand: string;
    image: string;
    price: string;
}

export interface SearchProdudctProps {
    products: SearchProduct[];
}