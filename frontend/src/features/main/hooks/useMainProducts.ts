import {useQuery} from '@tanstack/react-query';
import {useProductImageUrls} from '../../../hooks/useProductImageUrls';
import {getProducts} from '../../auction/services/AuctionService';
import type {AuctionItem} from '../../auction/types/AuctionItem';

const MAIN_QUERY_KEY = 'main';
const PRODUCT_SORT_OPTIONS = 'bidCount';
const PRODUCT_LIMIT = 4;
const PRODUCT_EMPTY: AuctionItem[] = [];

export const useMainProducts = () => {
    const {data: initialProducts, isLoading: isInitialLoading, isError} = useQuery({
        queryKey: [MAIN_QUERY_KEY],
        queryFn: () => getProducts({sortBy: PRODUCT_SORT_OPTIONS, limit: PRODUCT_LIMIT}),
        select: (response) => response.data,
    });

    const {productsWithImages, isLoadingImages} = useProductImageUrls(
        initialProducts || PRODUCT_EMPTY,
    );

    return {
        products: productsWithImages,
        isLoading: isInitialLoading || isLoadingImages,
        isError,
    };
};
