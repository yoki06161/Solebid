import type { AuctionListProps } from "../../types/auction/AuctionListProps";
import AuctionItem from "./AuctionItem";

const AuctionList = ({ items, onBidClick }: AuctionListProps) => {
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {items.map(item => (
                <AuctionItem
                    key={item.id}
                    item={item}
                    onBidClick={onBidClick}
                />
            ))}
        </div>
    );
};

export default AuctionList;