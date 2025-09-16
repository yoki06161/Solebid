import {useQuery} from '@tanstack/react-query';
import {getProducts} from '../../auction/services/AuctionService';
import {useProductImageUrls} from '../../../hooks/useProductImageUrls';
import type {AuctionItem} from '../../auction/types/AuctionItem';

const SEARCH_RANKING_QUERY_KEY = 'searchRanking';
const PRODUCT_SORT_OPTIONS = 'bidCount';
const PRODUCT_LIMIT = 3;
const PRODUCT_EMPTY: AuctionItem[] = [];

export const useSearchRanking = () => {
    const {data: initialProducts, isLoading: isInitialLoading, isError} = useQuery({
        queryKey: [SEARCH_RANKING_QUERY_KEY],
        queryFn: () => getProducts({sortBy: PRODUCT_SORT_OPTIONS, limit: PRODUCT_LIMIT}),
        select: (response) => response.data,
    });

    const {productsWithImages, isLoadingImages} = useProductImageUrls(
        initialProducts || PRODUCT_EMPTY,
    );

    return {
        data: productsWithImages,
        isLoading: isInitialLoading || isLoadingImages,
        isError,
    };
};
