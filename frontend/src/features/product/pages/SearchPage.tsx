import { useMemo, useState } from "react";
import Pagination from "../../../components/Pagination";
import { SearchHeader, SearchItem, SearchSidebar } from "../components/search";
import { allSearchResults, brands, sizes, sortOptions } from "../components/search/mockData";

const ITEMS_PER_PAGE = 6;

const SearchPage = () => {
    const [searchQuery] = useState("나이키");
    const [sortBy, setSortBy] = useState("인기순");
    const [showFilters, setShowFilters] = useState(false);
    const [priceRange, setPriceRange] = useState([0, 500000]);
    const [selectedBrands, setSelectedBrands] = useState<string[]>([]);
    const [selectedSizes, setSelectedSizes] = useState<string[]>([]);
    const [currentPage, setCurrentPage] = useState(1);

    const filteredResults = useMemo(() => {
        let results = [...allSearchResults];

        if (selectedBrands.length > 0) {
            results = results.filter((product) =>
                selectedBrands.includes(product.brand),
            );
        }

        results = results.filter((product) => {
            const price = parseInt(product.price.replace(/,/g, ""));
            return price >= priceRange[0] && price <= priceRange[1];
        });

        switch (sortBy) {
            case "가격 높은순":
                results.sort(
                    (a, b) =>
                        parseInt(b.price.replace(/,/g, ""))
                        -
                        parseInt(a.price.replace(/,/g, "")),
                );
                break;
            case "가격 낮은순":
                results.sort(
                    (a, b) =>
                        parseInt(a.price.replace(/,/g, ""))
                        -
                        parseInt(b.price.replace(/,/g, "")),
                );
                break;
            case "인기순":
            default:
                results.sort((a, b) => b.bidCount - a.bidCount);
                break;
        }
        return results;
    }, [selectedBrands, priceRange, sortBy]);

    const totalResults = filteredResults.length;
    const totalPages = Math.ceil(totalResults / ITEMS_PER_PAGE);
    const paginatedSearches = filteredResults.slice(
        (currentPage - 1) * ITEMS_PER_PAGE,
        currentPage * ITEMS_PER_PAGE
    );

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
                    totalResults={totalResults}
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
