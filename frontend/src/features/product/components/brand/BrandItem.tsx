import type { BrandItemProps } from "../../types/brand/BrandItemProps";

const BrandItem: React.FC<BrandItemProps> = ({ product, onBidClick }) => (
    <div className="bg-gray-50 rounded-lg p-4 cursor-pointer hover:shadow-md transition-shadow">
        <img
            src={product.image}
            alt={product.name}
            className="w-full h-48 object-cover rounded-lg mb-4"
        />
        <h4 className="text-lg font-medium mb-2">
            {product.name}
        </h4>
        <div className="flex justify-between items-center text-sm text-gray-600 mb-2">
            <span>
                현재 입찰가
            </span>
            <span className="font-semibold text-blue-600">
                ₩{product.currentBid}
            </span>
        </div>
        <div className="flex justify-between items-center text-sm text-gray-600 mb-4">
            <span>
                남은 시간
            </span>
            <span className="font-medium">
                {product.timeLeft}
            </span>
        </div>
        <div className="flex items-center justify-between">
            <span className="text-sm text-gray-500">
                {product.bidders}명 참여
            </span>
            <button
                onClick={() => onBidClick(product)}
                className="px-4 py-2 bg-blue-500 text-white !rounded-button hover:bg-blue-600 text-sm whitespace-nowrap"
            >
                입찰하기
            </button>
        </div>
    </div>
);

export default BrandItem;