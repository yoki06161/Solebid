export interface SearchRanking {
    id: number;
    rank: number;
    image: string;
    name: string;
    currentBid: number;
    bidders: number;
}

export interface SearchRankingProps {
    items: SearchRanking[];
}
