import {useQuery} from '@tanstack/react-query';
import {getProducts} from '../../auction/services/AuctionService';

const MAIN_QUERY_KEY = 'main';
const PRODUCT_SORT_OPTIONS = 'bidCount';
const PRODUCT_LIMIT = 4;

export const useMainProducts = () => {
    return useQuery({
        queryKey: [MAIN_QUERY_KEY],
        queryFn: () => getProducts({sortBy: PRODUCT_SORT_OPTIONS, limit: PRODUCT_LIMIT}),
        select: (response) => response.data,
    });
};
