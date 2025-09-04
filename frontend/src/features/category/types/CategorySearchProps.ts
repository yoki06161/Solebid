import type { CategoryOption } from "./CategoryOption";

export interface CategorySearchProps {
    brands: string[];
    priceRanges: CategoryOption[];
    sortOptions: CategoryOption[];
    selectedBrands: string[];
    handleBrandFilter: (brand: string) => void;
    selectedPriceRange: string;
    setSelectedPriceRange: React.Dispatch<React.SetStateAction<string>>;
    sortBy: string;
    setSortBy: React.Dispatch<React.SetStateAction<string>>;
}