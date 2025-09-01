import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { categories, heroImage, trendingProducts } from "./mockData";

function MainContent() {
    const [currentTab, setCurrentTab] = useState("trending");
    const navigate = useNavigate();
    return (
        <div className="min-h-screen bg-gray-50">
            {/* Hero Section */}
            <div className="relative bg-gradient-to-r from-gray-50 to-transparent overflow-hidden">
                <div className="max-w-[1440px] mx-auto px-6 py-16">
                    <div className="grid grid-cols-2 gap-8 items-center">
                        <div className="space-y-6">
                            <h2 className="text-4xl font-bold text-gray-900">
                                신발의 가치를 <br />
                                새롭게 발견하세요
                            </h2>
                            <p className="text-lg text-gray-600">
                                한정판 스니커즈부터 클래식 모델까지,
                                <br />
                                투명한 경매를 통해 원하는 신발을 만나보세요.
                            </p>
                        </div>
                        <div className="relative">
                            <img
                                src={heroImage}
                                alt="Hero"
                                className="w-full h-[400px] object-cover rounded-lg"
                            />
                        </div>
                    </div>
                </div>
            </div>
            {/* Categories */}
            <div className="max-w-[1440px] mx-auto px-6 py-12">
                <h3 className="text-xl font-semibold text-gray-900 mb-6">
                    카테고리
                </h3>
                <div className="grid grid-cols-4 gap-4">
                    {categories.map((category, index) => (
                        <div
                            key={index}
                            className="bg-white p-6 rounded-lg shadow-sm hover:shadow-md transition-shadow"
                            onClick={() => navigate(`/category/${category.name}`)}
                        >
                            <div className="flex items-center justify-between">
                                <div>
                                    <h4 className="text-lg font-medium text-gray-900">
                                        {category.name}
                                    </h4>
                                    <p className="text-sm text-gray-500 mt-1">
                                        {category.count}개의 상품
                                    </p>
                                </div>
                                <i className={`fas ${category.icon} text-2xl text-blue-500`} />
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            {/* Products */}
            <div className="max-w-[1440px] mx-auto px-6 pb-12">
                <div className="flex items-center justify-between mb-6">
                    <h3 className="text-xl font-semibold text-gray-900">
                        실시간 인기 경매
                    </h3>
                    <div className="flex space-x-4">
                        <button
                            onClick={() => setCurrentTab("trending")}
                            className={
                                `px-4 py-2 rounded-lg cursor-pointer whitespace-nowrap shadow-sm
                                ${currentTab === "trending"
                                    ? "bg-blue-500 text-white"
                                    : "bg-white text-gray-600"
                                }`
                            }
                        >
                            인기순
                        </button>
                        <button
                            onClick={() => setCurrentTab("ending")}
                            className={
                                `px-4 py-2 rounded-lg cursor-pointer whitespace-nowrap shadow-sm
                                 ${currentTab === "ending"
                                    ? "bg-blue-500 text-white"
                                    : "bg-white text-gray-600"
                                }`
                            }
                        >
                            마감임박
                        </button>
                    </div>
                </div>
                <div className="grid grid-cols-4 gap-6">
                    {trendingProducts.map((product, index) => (
                        <div
                            key={index}
                            className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer overflow-hidden"
                        >
                            <Link to="">
                                <img
                                    src={product.image}
                                    alt={product.name}
                                    className="w-full h-64 object-cover"
                                />
                            </Link>
                            <div className="p-4">
                                <h4 className="text-lg font-medium text-gray-900">
                                    {product.name}
                                </h4>
                                <div className="mt-2 flex items-center justify-between">
                                    <div>
                                        <p className="text-sm text-gray-500">
                                            현재가
                                        </p>
                                        <p className="text-lg font-semibold text-blue-600">
                                            ₩{product.price}
                                        </p>
                                    </div>
                                    <div className="text-right">
                                        <p className="text-sm text-gray-500">
                                            {product.bidCount}명 참여
                                        </p>
                                        <p className="text-sm font-medium text-red-500">
                                            {product.timeLeft} 남음
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default MainContent
