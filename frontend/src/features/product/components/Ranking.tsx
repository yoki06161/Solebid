import React, { useState } from "react";

const Ranking: React.FC = () => {
    const [activeTab, setActiveTab] = useState("products");

    const productRankings = [
        {
            rank: 1,
            image:
                "https://readdy.ai/api/search-image?query=premium%20luxury%20sneakers%20on%20minimal%20light%20background%20with%20soft%20shadows%20professional%20product%20photography%20high%20end%20commercial%20shot%20modern%20elegant%20design&width=100&height=100&seq=1&orientation=squarish",
            name: "나이키 덩크 로우 레트로",
            currentBid: "890,000",
            bidders: 156,
        },
        {
            rank: 2,
            image:
                "https://readdy.ai/api/search-image?query=stylish%20athletic%20sneakers%20on%20clean%20white%20background%20with%20subtle%20reflection%20premium%20product%20photography%20minimalist%20composition&width=100&height=100&seq=2&orientation=squarish",
            name: "조던 1 레트로 하이 OG",
            currentBid: "750,000",
            bidders: 142,
        },
        {
            rank: 3,
            image:
                "https://readdy.ai/api/search-image?query=designer%20running%20shoes%20on%20light%20neutral%20background%20professional%20studio%20lighting%20commercial%20product%20shot&width=100&height=100&seq=3&orientation=squarish",
            name: "아디다스 이지 부스트",
            currentBid: "680,000",
            bidders: 128,
        },
    ];

    const sellerRankings = [
        {
            rank: 1,
            image:
                "https://readdy.ai/api/search-image?query=professional%20business%20portrait%20headshot%20on%20neutral%20background%20clean%20modern%20minimal%20style&width=100&height=100&seq=4&orientation=squarish",
            nickname: "프리미엄슈즈",
            successRate: 98.5,
            trustScore: 4.9,
        },
        {
            rank: 2,
            image:
                "https://readdy.ai/api/search-image?query=confident%20business%20person%20portrait%20professional%20headshot%20studio%20lighting%20neutral%20background&width=100&height=100&seq=5&orientation=squarish",
            nickname: "스니커마스터",
            successRate: 97.2,
            trustScore: 4.8,
        },
        {
            rank: 3,
            image:
                "https://readdy.ai/api/search-image?query=friendly%20professional%20portrait%20natural%20lighting%20modern%20minimal%20background&width=100&height=100&seq=6&orientation=squarish",
            nickname: "슈즈컬렉터",
            successRate: 96.8,
            trustScore: 4.7,
        },
    ];

    const bidderRankings = [
        {
            rank: 1,
            image:
                "https://readdy.ai/api/search-image?query=young%20trendy%20person%20portrait%20modern%20lifestyle%20photography%20minimal%20background&width=100&height=100&seq=7&orientation=squarish",
            nickname: "킹오브슈즈",
            winCount: 87,
            totalAmount: "32,450,000",
        },
        {
            rank: 2,
            image:
                "https://readdy.ai/api/search-image?query=stylish%20individual%20portrait%20contemporary%20photography%20clean%20background&width=100&height=100&seq=8&orientation=squarish",
            nickname: "슈프림컬렉터",
            winCount: 75,
            totalAmount: "28,920,000",
        },
        {
            rank: 3,
            image:
                "https://readdy.ai/api/search-image?query=casual%20modern%20person%20portrait%20lifestyle%20photography%20simple%20background&width=100&height=100&seq=9&orientation=squarish",
            nickname: "스니커헌터",
            winCount: 68,
            totalAmount: "25,780,000",
        },
    ];

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-[1440px] mx-auto px-6 py-8">
                <div className="flex space-x-4 border-b border-gray-200 mb-8">
                    <button
                        onClick={() => setActiveTab("products")}
                        className={`px-6 py-4 font-medium !rounded-button whitespace-nowrap ${activeTab === "products"
                            ? "text-blue-600 border-b-2 border-blue-600"
                            : "text-gray-600"
                            }`}
                    >
                        상품 랭킹
                    </button>
                    <button
                        onClick={() => setActiveTab("sellers")}
                        className={`px-6 py-4 font-medium !rounded-button whitespace-nowrap ${activeTab === "sellers"
                            ? "text-blue-600 border-b-2 border-blue-600"
                            : "text-gray-600"
                            }`}
                    >
                        판매자 랭킹
                    </button>
                    <button
                        onClick={() => setActiveTab("bidders")}
                        className={`px-6 py-4 font-medium !rounded-button whitespace-nowrap ${activeTab === "bidders"
                            ? "text-blue-600 border-b-2 border-blue-600"
                            : "text-gray-600"
                            }`}
                    >
                        입찰자 랭킹
                    </button>
                </div>
                {activeTab === "products" && (
                    <div className="space-y-4">
                        {productRankings.map((item) => (
                            <a
                                key={item.rank}
                                href="https://readdy.ai/home/8c14b666-4886-429c-ad07-c16c2cd22c03/1a7c3882-3e81-42c2-9362-0f181f1f510e"
                                data-readdy="true"
                                className="block bg-white rounded-lg p-4 hover:shadow-md transition-shadow"
                            >
                                <div className="flex items-center space-x-6">
                                    <span className="text-3xl font-bold text-gray-900 w-12">
                                        {item.rank}
                                    </span>
                                    <img
                                        src={item.image}
                                        alt=""
                                        className="w-20 h-20 object-cover rounded-lg"
                                    />
                                    <div className="flex-1">
                                        <h3 className="text-lg font-medium text-gray-900">
                                            {item.name}
                                        </h3>
                                        <div className="flex items-center justify-between mt-2">
                                            <div>
                                                <p className="text-sm text-gray-500">현재 입찰가</p>
                                                <p className="text-lg font-semibold text-blue-600">
                                                    ₩{item.currentBid}
                                                </p>
                                            </div>
                                            <div className="text-right">
                                                <p className="text-sm text-gray-500">참여자 수</p>
                                                <p className="text-lg font-semibold text-gray-900">
                                                    {item.bidders}명
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </a>
                        ))}
                    </div>
                )}
                {activeTab === "sellers" && (
                    <div className="space-y-4">
                        {sellerRankings.map((item) => (
                            <a
                                key={item.rank}
                                href="https://readdy.ai/home/8c14b666-4886-429c-ad07-c16c2cd22c03/1a7c3882-3e81-42c2-9362-0f181f1f510e"
                                data-readdy="true"
                                className="block bg-white rounded-lg p-4 hover:shadow-md transition-shadow"
                            >
                                <div className="flex items-center space-x-6">
                                    <span className="text-3xl font-bold text-gray-900 w-12">
                                        {item.rank}
                                    </span>
                                    <img
                                        src={item.image}
                                        alt=""
                                        className="w-20 h-20 object-cover rounded-full"
                                    />
                                    <div className="flex-1">
                                        <h3 className="text-lg font-medium text-gray-900">
                                            {item.nickname}
                                        </h3>
                                        <div className="flex items-center justify-between mt-2">
                                            <div>
                                                <p className="text-sm text-gray-500">거래 성사율</p>
                                                <p className="text-lg font-semibold text-blue-600">
                                                    {item.successRate}%
                                                </p>
                                            </div>
                                            <div className="text-right">
                                                <p className="text-sm text-gray-500">신뢰도 점수</p>
                                                <div className="flex items-center justify-end mt-1">
                                                    {[...Array(5)].map((_, index) => (
                                                        <i
                                                            key={index}
                                                            className={`fas fa-star text-lg ${index < Math.floor(item.trustScore)
                                                                ? "text-yellow-400"
                                                                : "text-gray-300"
                                                                }`}
                                                        ></i>
                                                    ))}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </a>
                        ))}
                    </div>
                )}
                {activeTab === "bidders" && (
                    <div className="space-y-4">
                        {bidderRankings.map((item) => (
                            <a
                                key={item.rank}
                                href="https://readdy.ai/home/8c14b666-4886-429c-ad07-c16c2cd22c03/1a7c3882-3e81-42c2-9362-0f181f1f510e"
                                data-readdy="true"
                                className="block bg-white rounded-lg p-4 hover:shadow-md transition-shadow"
                            >
                                <div className="flex items-center space-x-6">
                                    <span className="text-3xl font-bold text-gray-900 w-12">
                                        {item.rank}
                                    </span>
                                    <img
                                        src={item.image}
                                        alt=""
                                        className="w-20 h-20 object-cover rounded-full"
                                    />
                                    <div className="flex-1">
                                        <h3 className="text-lg font-medium text-gray-900">
                                            {item.nickname}
                                        </h3>
                                        <div className="flex items-center justify-between mt-2">
                                            <div>
                                                <p className="text-sm text-gray-500">낙찰 횟수</p>
                                                <p className="text-lg font-semibold text-blue-600">
                                                    {item.winCount}회
                                                </p>
                                            </div>
                                            <div className="text-right">
                                                <p className="text-sm text-gray-500">총 거래금액</p>
                                                <p className="text-lg font-semibold text-gray-900">
                                                    ₩{item.totalAmount}
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </a>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Ranking;
