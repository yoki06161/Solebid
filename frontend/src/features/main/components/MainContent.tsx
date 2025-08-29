import { useState } from "react";
import { useNavigate } from "react-router-dom";

function MainContent() {
    const navigate = useNavigate();

    const [currentTab, setCurrentTab] = useState("trending");

    const heroImage =
        "https://readdy.ai/api/search-image?query=modern%20elegant%20luxury%20sneakers%20artistically%20arranged%20in%20minimal%20studio%20setting%20with%20soft%20gradient%20background%2C%20professional%20product%20photography%20with%20dramatic%20lighting%20and%20shadows&width=800&height=600&seq=1&orientation=landscape";

    const categories = [
        { name: "스니커즈", icon: "fa-shoe-prints", count: "2,431" },
        { name: "러닝화", icon: "fa-running", count: "1,523" },
        { name: "농구화", icon: "fa-basketball", count: "842" },
        { name: "캔버스화", icon: "fa-socks", count: "976" },
    ];

    const trendingProducts = [
        {
            image:
                "https://readdy.ai/api/search-image?query=premium%20white%20and%20blue%20athletic%20sneakers%20on%20minimal%20light%20background%20with%20subtle%20reflection%2C%20professional%20product%20photography&width=300&height=300&seq=2&orientation=squarish",
            name: "Nike Air Max 2025",
            price: "289,000",
            bidCount: 32,
            timeLeft: "2:14:53",
        },
        {
            image:
                "https://readdy.ai/api/search-image?query=luxury%20black%20and%20red%20sports%20sneakers%20on%20minimal%20light%20background%20with%20soft%20shadows%2C%20high-end%20product%20shot&width=300&height=300&seq=3&orientation=squarish",
            name: "Adidas Ultra Boost",
            price: "259,000",
            bidCount: 28,
            timeLeft: "1:45:21",
        },
        {
            image:
                "https://readdy.ai/api/search-image?query=premium%20gray%20and%20white%20running%20shoes%20on%20minimal%20light%20background%20with%20clean%20composition%2C%20professional%20studio%20photography&width=300&height=300&seq=4&orientation=squarish",
            name: "New Balance 990v6",
            price: "249,000",
            bidCount: 25,
            timeLeft: "3:22:15",
        },
        {
            image:
                "https://readdy.ai/api/search-image?query=trendy%20beige%20and%20white%20lifestyle%20sneakers%20on%20minimal%20light%20background%20with%20artistic%20shadows%2C%20commercial%20product%20photography&width=300&height=300&seq=5&orientation=squarish",
            name: "Converse Chuck 70",
            price: "129,000",
            bidCount: 18,
            timeLeft: "4:55:32",
        },
    ];

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
                <h3 className="text-xl font-semibold text-gray-900 mb-6">카테고리</h3>
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
                                <i
                                    className={`fas ${category.icon} text-2xl text-blue-500`}
                                ></i>
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
                            className={`px-4 py-2 !rounded-button cursor-pointer whitespace-nowrap ${currentTab === "trending"
                                ? "bg-blue-500 text-white"
                                : "bg-white text-gray-600"
                                }`}
                        >
                            인기순
                        </button>
                        <button
                            onClick={() => setCurrentTab("ending")}
                            className={`px-4 py-2 !rounded-button cursor-pointer whitespace-nowrap ${currentTab === "ending"
                                ? "bg-blue-500 text-white"
                                : "bg-white text-gray-600"
                                }`}
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
                            <a
                                href="https://readdy.ai/home/8c14b666-4886-429c-ad07-c16c2cd22c03/ad0cf9eb-0d08-4f31-aba6-a87043edcd2e"
                                data-readdy="true"
                            >
                                <img
                                    src={product.image}
                                    alt={product.name}
                                    className="w-full h-64 object-cover"
                                />
                            </a>
                            <div className="p-4">
                                <h4 className="text-lg font-medium text-gray-900">
                                    {product.name}
                                </h4>
                                <div className="mt-2 flex items-center justify-between">
                                    <div>
                                        <p className="text-sm text-gray-500">현재가</p>
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
