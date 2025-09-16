import {useQuery} from '@tanstack/react-query';
import {searchProducts} from '../services/SearchService';
import {useProductImageUrls} from '../../../hooks/useProductImageUrls';
import type {AuctionItem} from '../../auction/types/AuctionItem';

const PRODUCT_SEARCH_QUERY_KEY = 'productSearch';
const PRODUCT_EMPTY: AuctionItem[] = [];

export const useSearchProducts = (keyword: string) => {
    const {
        data: initialProducts,
        isLoading: isInitialLoading,
        isError,
        refetch,
    } = useQuery({
        queryKey: [PRODUCT_SEARCH_QUERY_KEY, keyword],
        queryFn: () => searchProducts(keyword),
        select: (response) => response.data,
        enabled: !!keyword,
    });

    const {productsWithImages, isLoadingImages} = useProductImageUrls(
        initialProducts || PRODUCT_EMPTY,
    );

    return {
        data: productsWithImages,
        isLoading: isInitialLoading || isLoadingImages,
        isError,
        refetch,
    };
};
