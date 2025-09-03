import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { SearchHeader, SearchHistory, SearchProduct, SearchRankingList } from "../components";
import { products, rankings, searches } from "../components/mockData";

const SearchPage = () => {
    const [searchQuery, setSearchQuery] = useState("");
    const [recentSearches, setRecentSearches] = useState(searches);

    const navigate = useNavigate();

    const filteredProducts = products.filter(
        (product) =>
            product.brand.toLowerCase().includes(searchQuery.toLowerCase()) ||
            product.name.toLowerCase().includes(searchQuery.toLowerCase()),
    );

    const handleGoBack = () => {
        navigate(-1);
    };

    const handleClearSearch = () => {
        setSearchQuery("");
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
                {
                    searchQuery
                        ? (
                            <SearchProduct
                                products={filteredProducts}
                            />
                        )
                        : (
                            <>
                                <SearchHistory
                                    recentSearches={recentSearches}
                                    handleRecentSearchClick={handleRecentSearchClick}
                                    handleRemoveRecentSearch={handleRemoveRecentSearch}
                                    handleClearAllRecentSearches={handleClearAllRecentSearches}
                                />
                                <SearchRankingList
                                    items={rankings}
                                />
                            </>
                        )
                }
            </div>
        </div>
    );
};

export default SearchPage;
