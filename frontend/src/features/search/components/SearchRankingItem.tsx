import { Link } from "react-router-dom";
import type { SearchRanking } from "../types/SearchRankingProps";

const SearchRankingItem = ({ rank, image, name, currentBid, bidders }: SearchRanking) => (
    <Link
        to=""
        className="block bg-white rounded-lg p-4 hover:shadow-md transition-shadow"
    >
        <div className="flex items-center space-x-6">
            <span className="text-xl font-bold text-gray-900 w-12">
                {rank}
            </span>
            <img
                src={image}
                alt={name}
                className="w-20 h-20 object-cover rounded-lg"
            />
            <div className="flex-1">
                <h3 className="text-base font-medium text-gray-900">
                    {name}
                </h3>
                <div className="flex items-center justify-between mt-2">
                    <div>
                        <p className="text-sm text-gray-500">
                            현재 입찰가
                        </p>
                        <p className="text-base font-semibold text-blue-600">
                            ₩{currentBid}
                        </p>
                    </div>
                    <div className="text-right">
                        <p className="text-sm text-gray-500">
                            참여자 수
                        </p>
                        <p className="text-base font-semibold text-gray-900">
                            {bidders}명
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </Link>
);

export default SearchRankingItem;