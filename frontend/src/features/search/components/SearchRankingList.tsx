import type { SearchRankingProps } from "../types/SearchRankingProps";
import SearchRankingItem from "./SearchRankingItem";

const SearchRankingList = ({ items }: SearchRankingProps) => (
    <div className="px-4 py-6 space-y-4">
        <h3 className="text-lg font-semibold">
            상품 랭킹
        </h3>
        {items.map((item) => (
            <SearchRankingItem
                key={item.rank}
                {...item}
            />
        ))}
    </div>
);

export default SearchRankingList;