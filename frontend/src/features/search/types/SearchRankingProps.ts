export interface SearchRanking {
    rank: number;
    image: string;
    name: string;
    currentBid: string;
    bidders: number;
}

export interface SearchRankingProps {
    items: SearchRanking[];
}