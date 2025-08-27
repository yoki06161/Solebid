import type { RankingSellerListProps } from "../../types/ranking/RankingSellerProps";
import RankingSellerItem from "./RankingSellerItem";

const RankingSellerList: React.FC<RankingSellerListProps> = ({ items }) => (
    <div className="space-y-4">
        {items.map((item) => (
            <RankingSellerItem
                key={item.rank}
                {...item}
            />
        ))}
    </div>
);

export default RankingSellerList;