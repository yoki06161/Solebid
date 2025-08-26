export interface RankingBidderProps {
    rank: number;
    image: string;
    nickname: string;
    winCount: number;
    totalAmount: string;
}

export interface RankingBidderListProps {
    items: RankingBidderProps[];
}