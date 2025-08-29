import { useMemo, useState } from "react";
import Pagination from "../../../components/Pagination";
import { usePagination } from "../../../hooks/usePagination";
import { SearchHeader, SearchItem, SearchSidebar } from "../components/search";
import { searchResults } from "../components/search/mockData";
import type { Search } from "../types/search/Search";

const SearchPage = () => {
    const [searchQuery] = useState("나이키");
    const [sortBy, setSortBy] = useState("인기순");
    const [showFilters, setShowFilters] = useState(false);
    const [priceRange, setPriceRange] = useState([0, 500000]);
    const [selectedBrands, setSelectedBrands] = useState<string[]>([]);
    const [selectedSizes, setSelectedSizes] = useState<number[]>([]);

    const brands = useMemo(() => [...new Set(searchResults.map((p) => p.brand))], []);
    const sizes = useMemo(() => [...new Set(searchResults.map((p) => p.size))].sort((a, b) => a - b), []);
    const sortOptions = ["인기순", "가격 높은순", "가격 낮은순"];

    const filteredResults = useMemo((): Search[] => {
        const sortFuns: Record<string, (a: Search, b: Search) => number> = {
            "가격 높은순": (a, b) => b.price - a.price,
            "가격 낮은순": (a, b) => a.price - b.price,
            "인기순": (a, b) => b.bidCount - a.bidCount,
        };
        
        const sortFunction = sortFuns[sortBy] || sortFuns["인기순"];

        return searchResults
            .filter((product: Search) => {
                const isInBrand = selectedBrands.length === 0 || selectedBrands.includes(product.brand);
                const isInPriceRange = product.price >= priceRange[0] && product.price <= priceRange[1];
                const isInSize = selectedSizes.length === 0 || selectedSizes.includes(product.size);
                return isInBrand && isInPriceRange && isInSize;
            })
            .sort(sortFunction);
    }, [selectedBrands, priceRange, sortBy, selectedSizes]);

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

    const handleSizeChange = (size: number) => {
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
                        sizes={sizes.map(s => s.toString())}
                        selectedSizes={selectedSizes.map(s => s.toString())}
                        handleSizeChange={(s) => handleSizeChange(parseInt(s))}
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
                        {filteredResults.length > 0 && (
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
