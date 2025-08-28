import type { WishListProps } from "../../types/wish/WishListProps";
import WishEmptyList from "./WishEmptyList";
import WishItem from "./WishItem";

const WishList: React.FC<WishListProps> = ({
    items,
    onRemove,
}) => {
    if (items.length === 0) {
        return <WishEmptyList />;
    }
    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {items.map((item) => (
                <WishItem
                    key={item.id}
                    item={item}
                    onRemove={onRemove}
                />
            ))}
        </div>
    );
};

export default WishList;
