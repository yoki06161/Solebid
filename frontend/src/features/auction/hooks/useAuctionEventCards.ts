import { useQuery } from '@tanstack/react-query';
import { fetchAuctionEventCards } from '../services/auctionEvents';
import type { AuctionEventCard } from '../types/AuctionEventCard';

const QUERY_KEY = ['auction-event-cards'];

export function useAuctionEventCards(limit = 30) {
    return useQuery<AuctionEventCard[], Error>({
        queryKey: [...QUERY_KEY, limit],
        queryFn: () => fetchAuctionEventCards(limit),
    });
}
