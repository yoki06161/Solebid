import { Link } from "react-router-dom";
import type { WishItemProps } from "../../types/wish/WishItemProps";

const WishItem: React.FC<WishItemProps> = ({
    item,
    onRemove,
}) => (
    <div className="bg-white rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow">
        <div className="relative">
            <img
                src={item.image}
                alt={item.name}
                className="w-full h-64 object-cover object-top cursor-pointer"
            />
            <button
                onClick={() => onRemove(item.id)}
                className="absolute top-3 right-3 w-8 h-8 bg-white rounded-full flex items-center justify-center shadow-md hover:bg-red-50 cursor-pointer"
            >
                <i className="fas fa-heart text-red-500 text-sm" />
            </button>
        </div>
        <div className="p-4">
            <h3 className="font-medium text-gray-900 mb-2 cursor-pointer hover:text-blue-600">
                {item.name}
            </h3>
            <p className="text-blue-600 font-semibold text-lg mb-1">
                {item.price.toLocaleString()}원
            </p>
            <p className="text-gray-500 text-xs mb-4">
                {item.dateAdded} 등록
            </p>
            <Link
                to="https://readdy.ai/home/..."
                className="flex justify-center w-full px-3 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 cursor-pointer !rounded-button whitespace-nowrap">
                입찰하기
            </Link>
        </div>
    </div>
);

export default WishItem;