import type { RankingProduct } from "../../types/ranking/RankingProductProps";

const RankingProductItem: React.FC<RankingProduct> = ({ rank, image, name, currentBid, bidders }) => (
    <a
        href="#"
        className="block bg-white rounded-lg p-4 hover:shadow-md transition-shadow"
    >
        <div className="flex items-center space-x-6">
            <span className="text-3xl font-bold text-gray-900 w-12">
                {rank}
            </span>
            <img
                src={image}
                alt={name}
                className="w-20 h-20 object-cover rounded-lg"
            />
            <div className="flex-1">
                <h3 className="text-lg font-medium text-gray-900">
                    {name}
                </h3>
                <div className="flex items-center justify-between mt-2">
                    <div>
                        <p className="text-sm text-gray-500">
                            현재 입찰가
                        </p>
                        <p className="text-lg font-semibold text-blue-600">
                            ₩{currentBid}
                        </p>
                    </div>
                    <div className="text-right">
                        <p className="text-sm text-gray-500">
                            참여자 수
                        </p>
                        <p className="text-lg font-semibold text-gray-900">
                            {bidders}명
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </a>
);

export default RankingProductItem;