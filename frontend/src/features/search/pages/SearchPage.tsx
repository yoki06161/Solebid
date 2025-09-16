import {Fragment, useMemo, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {SearchHeader, SearchHistory, SearchProduct, SearchRankingList,} from '../components';
import {products, searches} from '../components/mockData';
import {useSearchRanking} from '../hooks/useSearchRanking';
import type {SearchRanking} from '../types/SearchRankingProps';

const SearchPage = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [recentSearches, setRecentSearches] = useState(searches);

    const navigate = useNavigate();

    const {data: rankingProducts, isLoading, isError} = useSearchRanking();

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

    const filteredProducts = products.filter(
        (product) =>
            product.brand.toLowerCase().includes(searchQuery.toLowerCase()) ||
            product.name.toLowerCase().includes(searchQuery.toLowerCase()),
    );

    const handleGoBack = () => {
        navigate(-1);
    };

    const handleClearSearch = () => {
        setSearchQuery('');
    };

    const handleRecentSearchClick = (search: string) => {
        setSearchQuery(search);
    };

    const handleRemoveRecentSearch = (searchToRemove: string) => {
        setRecentSearches((prev) =>
            prev.filter((search) => search !== searchToRemove),
        );
    };

    const handleClearAllRecentSearches = () => {
        setRecentSearches([]);
    };

    if (isLoading) {
        return (
            <div className="fixed top-0 left-0 w-full h-full flex justify-center items-center">
                <i className="fas fa-spinner fa-spin fa-3x"></i>
            </div>
        );
    }

    if (isError) {
        return <div>Error: 데이터를 불러오는 중 오류가 발생했습니다.</div>;
    }

    return (
        <div className="bg-white w-full h-full overflow-hidden">
            <SearchHeader
                searchQuery={searchQuery}
                setSearchQuery={setSearchQuery}
                handleClearSearch={handleClearSearch}
                handleCloseModal={handleGoBack}
            />
            <div className="h-full overflow-y-auto pb-20">
                {searchQuery ? (
                    <SearchProduct products={filteredProducts}/>
                ) : (
                    <Fragment>
                        <SearchHistory
                            recentSearches={recentSearches}
                            handleRecentSearchClick={handleRecentSearchClick}
                            handleRemoveRecentSearch={handleRemoveRecentSearch}
                            handleClearAllRecentSearches={handleClearAllRecentSearches}
                        />
                        <SearchRankingList
                            items={rankings}
                        />
                    </Fragment>
                )}
            </div>
        </div>
    );
};

export default SearchPage;

