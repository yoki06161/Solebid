import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getFormatPrice } from '../../../utils/get-format-price';
import { SearchHeader, SearchHistory, SearchProduct, SearchRankingList } from '../components';
import { useSearchProducts } from '../hooks/useSearchProducts';
import { useSearchRanking } from '../hooks/useSearchRanking';
import type { SearchProduct as SearchProductType } from '../types/SearchProductProps';
import type { SearchRanking } from '../types/SearchRankingProps';

const SearchPage = () => {
    const navigate = useNavigate();

    const [searchQuery, setSearchQuery] = useState('');
    const [submittedQuery, setSubmittedQuery] = useState('');
    const [recentSearches, setRecentSearches] = useState<string[]>(() => {
        const storedSearches = localStorage.getItem('recentSearches');
        return storedSearches ? JSON.parse(storedSearches) : [];
    });

    useEffect(() => {
        localStorage.setItem('recentSearches', JSON.stringify(recentSearches));
    }, [recentSearches]);

    const {
        data: filteredProducts,
        isLoading: isSearchLoading,
        isError: isSearchError,
    } = useSearchProducts(submittedQuery);

    const {
        data: rankingProducts,
        isLoading: isRankingLoading,
        isError: isRankingError,
    } = useSearchRanking();

    const rankings = useMemo(() => {
        return (rankingProducts ?? []).map(
            (p, index): SearchRanking => ({
                id: p.id,
                rank: index + 1,
                image: p.imageUrl || p.image || '',
                name: p.name || '',
                currentBid: p.currentBid ?? 0,
                bidders: p.bidders,
            }),
        );
    }, [rankingProducts]);

    const searchResultProducts = useMemo((): SearchProductType[] => {
        if (!filteredProducts) return [];
        return filteredProducts.map((product) => ({
            id: product.id,
            name: product.name,
            brand: product.brand,
            image: product.imageUrl || product.image || '',
            price: getFormatPrice(product.currentBid ?? 0),
        }));
    }, [filteredProducts]);

    const handleGoBack = () => {
        navigate(-1);
    };

    const handleClearSearch = () => {
        setSearchQuery('');
        setSubmittedQuery('');
    };

    const handleRecentSearchClick = (search: string) => {
        setSearchQuery(search);
        setSubmittedQuery(search);
    };

    const handleRemoveRecentSearch = (searchToRemove: string) => {
        setRecentSearches((prev) =>
            prev.filter((search) => search !== searchToRemove),
        );
    };

    const handleClearAllRecentSearches = () => {
        setRecentSearches([]);
    };

    const handleSearch = () => {
        const trimmedQuery = searchQuery.trim();
        if (trimmedQuery === '') {
            return;
        }
        
        setSubmittedQuery(trimmedQuery);

        const updatedRecentSearches = [
            trimmedQuery,
            ...recentSearches.filter((s) => s !== trimmedQuery),
        ];

        setRecentSearches(updatedRecentSearches);
    };

    const renderContent = () => {
        if (submittedQuery) {
            if (isSearchLoading) {
                return (
                    <div className="flex justify-center items-center pt-10">
                        <i className="fas fa-spinner fa-spin fa-3x" />
                    </div>
                );
            }
            return <SearchProduct products={searchResultProducts} />;
        }

        if (isRankingLoading) {
            return (
                <div className="flex justify-center items-center pt-10">
                    <i className="fas fa-spinner fa-spin fa-3x" />
                </div>
            );
        }

        return <SearchRankingList items={rankings} />;
    };

    if (isRankingError || isSearchError) {
        return <div>Error: 데이터를 불러오는 중 오류가 발생했습니다.</div>;
    }

    return (
        <div className="bg-white w-full h-full overflow-hidden">
            <SearchHeader
                searchQuery={searchQuery}
                setSearchQuery={setSearchQuery}
                handleClearSearch={handleClearSearch}
                handleCloseModal={handleGoBack}
                handleSearch={handleSearch}
            />
            <SearchHistory
                recentSearches={recentSearches}
                handleRecentSearchClick={handleRecentSearchClick}
                handleRemoveRecentSearch={handleRemoveRecentSearch}
                handleClearAllRecentSearches={handleClearAllRecentSearches}
            />
            <div className="h-full overflow-y-auto pb-20">
                {renderContent()}
            </div>
        </div>
    );
};

export default SearchPage;
