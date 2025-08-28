import { useMemo, useState } from "react";
import Pagination from "../../../components/Pagination";
import { usePagination } from "../../../hooks/usePagination";
import { SearchHeader, SearchItem, SearchSidebar } from "../components/search";
import { allSearchResults, brands, sizes, sortOptions } from "../components/search/mockData";
import type { Search } from "../types/search/Search";

const SearchPage = () => {
    const [searchQuery] = useState("나이키");
    const [sortBy, setSortBy] = useState("인기순");
    const [showFilters, setShowFilters] = useState(false);
    const [priceRange, setPriceRange] = useState([0, 500000]);
    const [selectedBrands, setSelectedBrands] = useState<string[]>([]);
    const [selectedSizes, setSelectedSizes] = useState<string[]>([]);

    const getPrice = (priceString: string): number =>
        parseInt(priceString.replace(/,/g, ""), 10);

    const filteredResults = useMemo((): Search[] => {
        const sortFuns: Record<string, (a: Search, b: Search) => number> = {
            "가격 높은순": (a, b) => getPrice(b.price) - getPrice(a.price),
            "가격 낮은순": (a, b) => getPrice(a.price) - getPrice(b.price),
            "인기순": (a, b) => b.bidCount - a.bidCount,
        };
        const sortFunction = sortFuns[sortBy] || sortFuns["인기순"];

        return allSearchResults
            .filter((product: Search) => {
                const isInBrand = selectedBrands.length === 0 || selectedBrands.includes(product.brand);
                const price = getPrice(product.price);
                const isInPriceRange = price >= priceRange[0] && price <= priceRange[1];
                return isInBrand && isInPriceRange;
            })
            .sort(sortFunction);
    }, [selectedBrands, priceRange, sortBy]);

    const {
        paginatedData: paginatedSearches,
        currentPage,
        setCurrentPage,
        totalPages
    } = usePagination({ data: filteredResults, itemsPerPage: 6 });

    const handleBrandChange = (brand: string) => {
        setSelectedBrands((prev) =>
            prev.includes(brand)
                ? prev.filter((b) => b !== brand)
                : [...prev, brand],
        );
        setCurrentPage(1);
    };

    const handleSizeChange = (size: string) => {
        setSelectedSizes((prev) =>
            prev.includes(size) ? prev.filter((s) => s !== size) : [...prev, size],
        );
    };

    const resetFilters = () => {
        setPriceRange([0, 500000]);
        setSelectedBrands([]);
        setSelectedSizes([]);
        setSortBy("인기순");
        setCurrentPage(1);
    };

    const handleSortBy = (option: string) => {
        setSortBy(option);
        setCurrentPage(1);
    }

    return (
        <main className="min-h-screen bg-gray-50">
            <div className="max-w-[1440px] mx-auto px-6 py-8">
                <SearchHeader
                    searchQuery={searchQuery}
                    totalResults={filteredResults.length}
                    sortBy={sortBy}
                    setSortBy={handleSortBy}
                    sortOptions={sortOptions}
                    showFilters={showFilters}
                    setShowFilters={setShowFilters}
                />
                <div className="flex gap-8">
                    <SearchSidebar
                        showFilters={showFilters}
                        priceRange={priceRange}
                        setPriceRange={setPriceRange}
                        brands={brands}
                        selectedBrands={selectedBrands}
                        handleBrandChange={handleBrandChange}
                        sizes={sizes}
                        selectedSizes={selectedSizes}
                        handleSizeChange={handleSizeChange}
                        resetFilters={resetFilters}
                    />
                    <div className="flex-1">
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                            {paginatedSearches.map((product, index) => (
                                <SearchItem
                                    key={index}
                                    product={product}
                                />
                            ))}
                        </div>
                        {totalPages > 0 && (
                            <Pagination
                                currentPage={currentPage}
                                totalPages={totalPages}
                                onPageChange={setCurrentPage}
                            />
                        )}
                    </div>
                </div>
            </div>
        </main>
    );
};

export default SearchPage;
