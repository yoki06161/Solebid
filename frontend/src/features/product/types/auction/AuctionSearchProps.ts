export interface AuctionSearchProps {
    categories: string[];
    selectedCategory: string;
    onCategoryChange: (category: string) => void;
    sortOptions: string[];
    sortOption: string;
    onSortChange: (option: string) => void;
    priceRange: number[];
    onPriceChange: (value: number[]) => void;
}