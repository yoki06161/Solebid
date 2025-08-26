export interface RankingProduct {
    rank: number;
    image: string;
    name: string;
    currentBid: string;
    bidders: number;
}

export interface RankingProductListProps {
    items: RankingProduct[];
}