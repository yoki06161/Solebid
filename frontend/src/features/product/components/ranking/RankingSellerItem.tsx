import type { RankingSeller } from "../../types/ranking/RankingSellerProps";

const RankingSellerItem: React.FC<RankingSeller> = ({ rank, image, nickname, successRate, trustScore }) => (
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
                alt={nickname}
                className="w-20 h-20 object-cover rounded-full"
            />
            <div className="flex-1">
                <h3 className="text-lg font-medium text-gray-900">
                    {nickname}
                </h3>
                <div className="flex items-center justify-between mt-2">
                    <div>
                        <p className="text-sm text-gray-500">거래 성사율</p>
                        <p className="text-lg font-semibold text-blue-600">
                            {successRate}%
                        </p>
                    </div>
                    <div className="text-right">
                        <p className="text-sm text-gray-500">
                            신뢰도 점수
                        </p>
                        <div className="flex items-center justify-end mt-1">
                            {[...Array(5)].map((_, index) => (
                                <i
                                    key={index}
                                    className={`fas fa-star text-lg ${index < Math.round(trustScore) ? "text-yellow-400" : "text-gray-300"}`}
                                >
                                </i>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </a>
);

export default RankingSellerItem;