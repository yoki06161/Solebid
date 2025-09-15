import {useQuery} from '@tanstack/react-query';
import {getProducts} from "../../auction/services/AuctionService.tsx";

const SEARCH_RANKING_QUERY_KEY = 'searchRanking';
const PRODUCT_SORT_OPTIONS = 'bidCount';
const PRODUCT_LIMIT = 3;

export const useSearchRanking = () => {
    return useQuery({
        queryKey: [SEARCH_RANKING_QUERY_KEY],
        queryFn: () => getProducts({sortBy: PRODUCT_SORT_OPTIONS, limit: PRODUCT_LIMIT}),
        select: (response) => response.data,
    });
};
