import { apiFetch } from '../../../utils/apiFetch';
import type { AuctionEventCard } from '../types/AuctionEventCard';

export async function fetchAuctionEventCards(limit = 30) {
    const qs = new URLSearchParams();
    if (limit != null) qs.set('limit', String(limit));
    const suffix = qs.toString();
    const url = suffix ? `/api/auction-events/cards?${suffix}` : '/api/auction-events/cards';
    return apiFetch<AuctionEventCard[]>(url);
}
