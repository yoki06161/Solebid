export interface RankingSeller {
    rank: number;
    image: string;
    nickname: string;
    successRate: number;
    trustScore: number;
}

export interface RankingSellerListProps {
    items: RankingSeller[];
}