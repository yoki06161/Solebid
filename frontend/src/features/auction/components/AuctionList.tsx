import type { AuctionListProps } from "../types/AuctionListProps";
import AuctionItem from "./AuctionItem";

const AuctionList = ({ items, onBidClick, onSelect, addWish, removeWish, isAdding, isRemoving }: AuctionListProps) => {
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {items.map(item => (
                <AuctionItem
                    key={item.id}
                    item={item}
                    addWish={addWish}
                    removeWish={removeWish}
                    isAdding={isAdding}
                    isRemoving={isRemoving}
                    onBidClick={onBidClick}
                    onSelect={onSelect}
                />
            ))}
        </div>
    );
};

export default AuctionList;
