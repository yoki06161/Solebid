import React, { useState } from "react";

interface ProductType {
    id: number,
    name: string,
    image: string,
    currentBid: string,
    timeLeft: string,
    bidders: number
};

const Brand: React.FC = () => {
    const [selectedProduct, setSelectedProduct] = useState<ProductType | null>(null);
    const [bidAmount, setBidAmount] = useState<string>("");
    const [showModal, setShowModal] = useState<boolean>(false);
    const [error, setError] = useState<string>("");

    const handleBidClick = (product: ProductType) => {
        setSelectedProduct(product);
        setBidAmount("");
        setError("");
        setShowModal(true);
    };

    const handleBidSubmit = () => {
        const currentBid = parseInt(selectedProduct!.currentBid.replace(/,/g, ""));
        const newBid = parseInt(bidAmount.replace(/,/g, ""));
        const minBidIncrement = 10000; // 최소 입찰 단위: 1만원

        if (!bidAmount || isNaN(newBid)) {
            setError("유효한 금액을 입력해주세요.");
            return;
        }

        if (newBid <= currentBid) {
            setError("현재 입찰가보다 높은 금액을 입력해주세요.");
            return;
        }

        if (newBid - currentBid < minBidIncrement) {
            setError(`최소 입찰 단위는 ${minBidIncrement.toLocaleString()}원입니다.`);
            return;
        }

        // Here you would typically make an API call to submit the bid
        alert("입찰이 완료되었습니다."); // In a real app, replace this with proper feedback
        setShowModal(false);
    };

    const popularBrands = [
        {
            id: 1,
            name: "나이키",
            logo: "https://readdy.ai/api/search-image?query=minimal%20modern%20nike%20logo%20design%20on%20clean%20white%20background%20professional%20branding%20photography%20elegant%20commercial%20shot%20with%20soft%20shadows&width=120&height=120&seq=10&orientation=squarish",
        },
        {
            id: 2,
            name: "아디다스",
            logo: "https://readdy.ai/api/search-image?query=minimal%20modern%20adidas%20logo%20design%20on%20clean%20white%20background%20professional%20branding%20photography%20elegant%20commercial%20shot%20with%20soft%20shadows&width=120&height=120&seq=11&orientation=squarish",
        },
        {
            id: 3,
            name: "뉴발란스",
            logo: "https://readdy.ai/api/search-image?query=minimal%20modern%20new%20balance%20logo%20design%20on%20clean%20white%20background%20professional%20branding%20photography%20elegant%20commercial%20shot%20with%20soft%20shadows&width=120&height=120&seq=12&orientation=squarish",
        },
        {
            id: 4,
            name: "컨버스",
            logo: "https://readdy.ai/api/search-image?query=minimal%20modern%20converse%20logo%20design%20on%20clean%20white%20background%20professional%20branding%20photography%20elegant%20commercial%20shot%20with%20soft%20shadows&width=120&height=120&seq=13&orientation=squarish",
        },
        {
            id: 5,
            name: "반스",
            logo: "https://readdy.ai/api/search-image?query=minimal%20modern%20vans%20logo%20design%20on%20clean%20white%20background%20professional%20branding%20photography%20elegant%20commercial%20shot%20with%20soft%20shadows&width=120&height=120&seq=14&orientation=squarish",
        },
        {
            id: 6,
            name: "푸마",
            logo: "https://readdy.ai/api/search-image?query=minimal%20modern%20puma%20logo%20design%20on%20clean%20white%20background%20professional%20branding%20photography%20elegant%20commercial%20shot%20with%20soft%20shadows&width=120&height=120&seq=15&orientation=squarish",
        },
        {
            id: 7,
            name: "리복",
            logo: "https://readdy.ai/api/search-image?query=minimal%20modern%20reebok%20logo%20design%20on%20clean%20white%20background%20professional%20branding%20photography%20elegant%20commercial%20shot%20with%20soft%20shadows&width=120&height=120&seq=16&orientation=squarish",
        },
        {
            id: 8,
            name: "아식스",
            logo: "https://readdy.ai/api/search-image?query=minimal%20modern%20asics%20logo%20design%20on%20clean%20white%20background%20professional%20branding%20photography%20elegant%20commercial%20shot%20with%20soft%20shadows&width=120&height=120&seq=17&orientation=squarish",
        },
    ];

    const brandProducts = [
        {
            brand: "나이키",
            description: "혁신적인 디자인과 최고의 기술력",
            logo: "https://readdy.ai/api/search-image?query=minimal%20modern%20nike%20logo%20design%20on%20clean%20white%20background%20professional%20branding%20photography%20elegant%20commercial%20shot%20with%20soft%20shadows&width=80&height=80&seq=18&orientation=squarish",
            products: [
                {
                    id: 1,
                    name: "나이키 에어맥스 97",
                    image:
                        "https://readdy.ai/api/search-image?query=nike%20air%20max%2097%20sneaker%20on%20minimal%20light%20background%20professional%20product%20photography%20with%20soft%20shadows%20high%20end%20commercial%20shot&width=400&height=300&seq=19&orientation=landscape",
                    currentBid: "450,000",
                    timeLeft: "2시간 32분",
                    bidders: 23,
                },
                {
                    id: 2,
                    name: "나이키 줌 X",
                    image:
                        "https://readdy.ai/api/search-image?query=nike%20zoom%20x%20sneaker%20on%20minimal%20light%20background%20professional%20product%20photography%20with%20soft%20shadows%20high%20end%20commercial%20shot&width=400&height=300&seq=20&orientation=landscape",
                    currentBid: "380,000",
                    timeLeft: "4시간 15분",
                    bidders: 18,
                },
                {
                    id: 3,
                    name: "나이키 덩크 로우",
                    image:
                        "https://readdy.ai/api/search-image?query=nike%20dunk%20low%20sneaker%20on%20minimal%20light%20background%20professional%20product%20photography%20with%20soft%20shadows%20high%20end%20commercial%20shot&width=400&height=300&seq=21&orientation=landscape",
                    currentBid: "290,000",
                    timeLeft: "1시간 45분",
                    bidders: 31,
                },
            ],
        },
    ];

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-[1440px] mx-auto px-6 pt-24 pb-12">
                <section className="mb-12">
                    <h2 className="text-2xl font-bold mb-8">인기 브랜드</h2>
                    <div className="grid grid-cols-4 gap-6">
                        {popularBrands.map((brand) => (
                            <a
                                key={brand.id}
                                href="https://readdy.ai/home/8c14b666-4886-429c-ad07-c16c2cd22c03/ff6ab7ab-76f9-47f8-b166-840ccaa75262"
                                data-readdy="true"
                                className="bg-white rounded-xl p-6 flex flex-col items-center cursor-pointer hover:shadow-lg transition-shadow"
                            >
                                <img
                                    src={brand.logo}
                                    alt={brand.name}
                                    className="w-24 h-24 object-contain mb-4"
                                />
                                <h3 className="text-lg font-medium text-gray-900">
                                    {brand.name}
                                </h3>
                            </a>
                        ))}
                    </div>
                </section>
                <section>
                    <h2 className="text-2xl font-bold mb-8">브랜드별 상품</h2>
                    {brandProducts.map((brand) => (
                        <div key={brand.brand} className="bg-white rounded-xl p-8 mb-8">
                            <div className="flex items-center mb-6">
                                <img
                                    src={brand.logo}
                                    alt={brand.brand}
                                    className="w-16 h-16 object-contain mr-4"
                                />
                                <div>
                                    <h3 className="text-xl font-bold text-gray-900">
                                        {brand.brand}
                                    </h3>
                                    <p className="text-gray-600">{brand.description}</p>
                                </div>
                            </div>
                            <div className="grid grid-cols-3 gap-6">
                                {brand.products.map((product) => (
                                    <div
                                        key={product.id}
                                        className="bg-gray-50 rounded-lg p-4 cursor-pointer hover:shadow-md transition-shadow"
                                    >
                                        <img
                                            src={product.image}
                                            alt={product.name}
                                            className="w-full h-48 object-cover rounded-lg mb-4"
                                        />
                                        <h4 className="text-lg font-medium mb-2">{product.name}</h4>
                                        <div className="flex justify-between items-center text-sm text-gray-600 mb-2">
                                            <span>현재 입찰가</span>
                                            <span className="font-semibold text-blue-600">
                                                ₩{product.currentBid}
                                            </span>
                                        </div>
                                        <div className="flex justify-between items-center text-sm text-gray-600 mb-4">
                                            <span>남은 시간</span>
                                            <span className="font-medium">{product.timeLeft}</span>
                                        </div>
                                        <div className="flex items-center justify-between">
                                            <span className="text-sm text-gray-500">
                                                {product.bidders}명 참여
                                            </span>
                                            <button
                                                onClick={() => handleBidClick(product)}
                                                className="px-4 py-2 bg-blue-500 text-white !rounded-button hover:bg-blue-600 text-sm whitespace-nowrap"
                                            >
                                                입찰하기
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </section>
            </div>
            {showModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl p-6 w-full max-w-md">
                        <div className="flex justify-between items-center mb-4">
                            <h3 className="text-xl font-bold">입찰하기</h3>
                            <button
                                onClick={() => setShowModal(false)}
                                className="text-gray-500 hover:text-gray-700"
                            >
                                <i className="fas fa-times"></i>
                            </button>
                        </div>
                        {selectedProduct && (
                            <>
                                <div className="mb-4">
                                    <p className="text-gray-600 mb-2">상품명</p>
                                    <p className="font-medium">{selectedProduct.name}</p>
                                </div>
                                <div className="mb-4">
                                    <p className="text-gray-600 mb-2">현재 최고 입찰가</p>
                                    <p className="font-medium text-blue-600">
                                        ₩{selectedProduct.currentBid}
                                    </p>
                                </div>
                                <div className="mb-4">
                                    <p className="text-gray-600 mb-2">최소 입찰 단위</p>
                                    <p className="font-medium">₩10,000</p>
                                </div>
                                <div className="mb-4">
                                    <label className="block text-gray-600 mb-2">입찰 금액</label>
                                    <div className="relative">
                                        <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                                            ₩
                                        </span>
                                        <input
                                            type="text"
                                            value={bidAmount}
                                            onChange={(e) => {
                                                const value = e.target.value.replace(/[^0-9]/g, "");
                                                if (value === "" || /^\d+$/.test(value)) {
                                                    setBidAmount(
                                                        value.replace(/\B(?=(\d{3})+(?!\d))/g, ","),
                                                    );
                                                }
                                            }}
                                            className="w-full pl-8 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                            placeholder="금액을 입력하세요"
                                        />
                                    </div>
                                    {error && (
                                        <p className="text-red-500 text-sm mt-1">{error}</p>
                                    )}
                                </div>
                                <button
                                    onClick={handleBidSubmit}
                                    className="w-full px-4 py-2 bg-blue-500 text-white !rounded-button hover:bg-blue-600 whitespace-nowrap"
                                >
                                    입찰 확인
                                </button>
                            </>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default Brand;
