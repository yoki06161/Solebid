import type { RankingBidderProps } from "../../types/ranking/RankingBidderProps";

const RankingBidderItem: React.FC<RankingBidderProps> = ({ rank, image, nickname, winCount, totalAmount }) => (
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
                        <p className="text-sm text-gray-500">
                            낙찰 횟수
                        </p>
                        <p className="text-lg font-semibold text-blue-600">
                            {winCount}회
                        </p>
                    </div>
                    <div className="text-right">
                        <p className="text-sm text-gray-500">
                            총 거래금액
                        </p>
                        <p className="text-lg font-semibold text-gray-900">
                            ₩{totalAmount}
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </a>
);

export default RankingBidderItem;