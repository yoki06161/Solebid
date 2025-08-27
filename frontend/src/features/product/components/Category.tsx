import { useState } from "react";
import { useParams } from 'react-router-dom';

function Category() {
    const [selectedBrands, setSelectedBrands] = useState<string[]>([]);
    const [selectedPriceRange, setSelectedPriceRange] = useState<string>("");
    const [sortBy, setSortBy] = useState<string>("popular");
    const [currentPage, setCurrentPage] = useState(1);

    const { categoryName } = useParams();

    const decodedCategory = categoryName ? decodeURIComponent(categoryName) : '';

    const brands = ["Nike", "Adidas", "New Balance", "Puma", "Converse", "Vans"];

    const priceRanges = [
        { value: "0-100000", label: "10만원 이하" },
        { value: "100000-200000", label: "10-20만원" },
        { value: "200000-300000", label: "20-30만원" },
        { value: "300000-500000", label: "30-50만원" },
        { value: "500000+", label: "50만원 이상" },
    ];

    const sortOptions = [
        { value: "popular", label: "인기순" },
        { value: "ending", label: "마감임박순" },
        { value: "newest", label: "최신순" },
        { value: "price-low", label: "낮은가격순" },
        { value: "price-high", label: "높은가격순" },
    ];

    const sneakers = [
        {
            id: 1,
            image:
                "https://readdy.ai/api/search-image?query=premium%20Nike%20Air%20Jordan%201%20black%20and%20red%20basketball%20sneakers%20on%20clean%20white%20background%20with%20professional%20studio%20lighting%20and%20minimal%20shadows%20for%20product%20photography&width=300&height=300&seq=20&orientation=squarish",
            brand: "Nike",
            model: "Air Jordan 1 Retro High",
            currentBid: "289,000",
            timeLeft: "2:15:33",
            bidders: 42,
        },
        {
            id: 2,
            image:
                "https://readdy.ai/api/search-image?query=luxury%20Adidas%20Yeezy%20350%20V2%20cream%20white%20lifestyle%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=21&orientation=squarish",
            brand: "Adidas",
            model: "Yeezy Boost 350 V2",
            currentBid: "459,000",
            timeLeft: "1:42:18",
            bidders: 67,
        },
        {
            id: 3,
            image:
                "https://readdy.ai/api/search-image?query=premium%20New%20Balance%20990v5%20gray%20and%20navy%20running%20sneakers%20on%20clean%20white%20background%20with%20studio%20lighting%20and%20minimal%20shadows%20for%20product%20photography&width=300&height=300&seq=22&orientation=squarish",
            brand: "New Balance",
            model: "990v5 Made in USA",
            currentBid: "219,000",
            timeLeft: "4:33:07",
            bidders: 28,
        },
        {
            id: 4,
            image:
                "https://readdy.ai/api/search-image?query=trendy%20Nike%20Dunk%20Low%20panda%20black%20and%20white%20skateboarding%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=23&orientation=squarish",
            brand: "Nike",
            model: "Dunk Low Panda",
            currentBid: "189,000",
            timeLeft: "3:21:45",
            bidders: 35,
        },
        {
            id: 5,
            image:
                "https://readdy.ai/api/search-image?query=premium%20Adidas%20Stan%20Smith%20white%20and%20green%20tennis%20sneakers%20on%20clean%20white%20background%20with%20studio%20lighting%20and%20minimal%20shadows%20for%20product%20photography&width=300&height=300&seq=24&orientation=squarish",
            brand: "Adidas",
            model: "Stan Smith Original",
            currentBid: "129,000",
            timeLeft: "6:18:22",
            bidders: 19,
        },
        {
            id: 6,
            image:
                "https://readdy.ai/api/search-image?query=luxury%20Puma%20Suede%20Classic%20burgundy%20and%20white%20lifestyle%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=25&orientation=squarish",
            brand: "Puma",
            model: "Suede Classic XXI",
            currentBid: "99,000",
            timeLeft: "5:44:11",
            bidders: 15,
        },
        {
            id: 7,
            image:
                "https://readdy.ai/api/search-image?query=trendy%20Converse%20Chuck%20Taylor%20All%20Star%20high%20top%20black%20canvas%20sneakers%20on%20clean%20white%20background%20with%20studio%20lighting%20and%20minimal%20shadows&width=300&height=300&seq=26&orientation=squarish",
            brand: "Converse",
            model: "Chuck Taylor All Star High",
            currentBid: "79,000",
            timeLeft: "7:12:33",
            bidders: 12,
        },
        {
            id: 8,
            image:
                "https://readdy.ai/api/search-image?query=premium%20Vans%20Old%20Skool%20black%20and%20white%20skateboarding%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=27&orientation=squarish",
            brand: "Vans",
            model: "Old Skool Classic",
            currentBid: "119,000",
            timeLeft: "2:55:17",
            bidders: 23,
        },
        {
            id: 9,
            image:
                "https://readdy.ai/api/search-image?query=luxury%20Nike%20Air%20Max%2090%20white%20and%20gray%20running%20sneakers%20on%20clean%20white%20background%20with%20studio%20lighting%20and%20minimal%20shadows%20for%20product%20photography&width=300&height=300&seq=28&orientation=squarish",
            brand: "Nike",
            model: "Air Max 90 Essential",
            currentBid: "169,000",
            timeLeft: "1:33:28",
            bidders: 31,
        },
        {
            id: 10,
            image:
                "https://readdy.ai/api/search-image?query=premium%20Adidas%20Ultraboost%2022%20blue%20and%20white%20running%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=29&orientation=squarish",
            brand: "Adidas",
            model: "Ultraboost 22",
            currentBid: "239,000",
            timeLeft: "4:07:55",
            bidders: 38,
        },
        {
            id: 11,
            image:
                "https://readdy.ai/api/search-image?query=trendy%20New%20Balance%20327%20orange%20and%20navy%20retro%20sneakers%20on%20clean%20white%20background%20with%20studio%20lighting%20and%20minimal%20shadows%20for%20product%20photography&width=300&height=300&seq=30&orientation=squarish",
            brand: "New Balance",
            model: "327 Retro",
            currentBid: "149,000",
            timeLeft: "3:48:12",
            bidders: 26,
        },
        {
            id: 12,
            image:
                "https://readdy.ai/api/search-image?query=luxury%20Puma%20RS-X%20white%20and%20multicolor%20chunky%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=31&orientation=squarish",
            brand: "Puma",
            model: "RS-X Reinvention",
            currentBid: "179,000",
            timeLeft: "6:25:41",
            bidders: 29,
        },
    ];

    const handleBrandFilter = (brand: string) => {
        setSelectedBrands((prev) =>
            prev.includes(brand) ? prev.filter((b) => b !== brand) : [...prev, brand],
        );
    };

    const totalPages = Math.ceil(sneakers.length / 12);

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Breadcrumb */}
            <div className="max-w-[1440px] mx-auto px-6 py-4">
                <div className="flex items-center space-x-2 text-sm text-gray-500">
                    <a
                        href="https://readdy.ai/home/8c14b666-4886-429c-ad07-c16c2cd22c03/ad0cf9eb-0d08-4f31-aba6-a87043edcd2e"
                        data-readdy="true"
                        className="hover:text-gray-700 cursor-pointer"
                    >
                        홈
                    </a>
                    <i className="fas fa-chevron-right text-xs"></i>
                    <span className="text-gray-900">{decodedCategory}</span>
                </div>
            </div>
            <div className="max-w-[1440px] mx-auto px-6 pb-12">
                {/* Page Header */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">
                        {decodedCategory} 경매
                    </h1>
                    <p className="text-gray-600">
                        다양한 브랜드의 프리미엄 {decodedCategory}를 경매로 만나보세요
                    </p>
                </div>
                {/* Filters and Sort */}
                <div className="bg-white rounded-lg shadow-sm p-6 mb-8">
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        {/* Brand Filter */}
                        <div>
                            <h3 className="text-sm font-medium text-gray-900 mb-3">브랜드</h3>
                            <div className="flex flex-wrap gap-2">
                                {brands.map((brand) => (
                                    <button
                                        key={brand}
                                        onClick={() => handleBrandFilter(brand)}
                                        className={`px-3 py-1 text-sm !rounded-button border cursor-pointer whitespace-nowrap ${selectedBrands.includes(brand)
                                            ? "bg-blue-500 text-white border-blue-500"
                                            : "bg-white text-gray-700 border-gray-300 hover:border-blue-500"
                                            }`}
                                    >
                                        {brand}
                                    </button>
                                ))}
                            </div>
                        </div>
                        {/* Price Range Filter */}
                        <div>
                            <h3 className="text-sm font-medium text-gray-900 mb-3">가격대</h3>
                            <div className="relative">
                                <select
                                    value={selectedPriceRange}
                                    onChange={(e) => setSelectedPriceRange(e.target.value)}
                                    className="w-full px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer appearance-none"
                                >
                                    <option value="">전체 가격대</option>
                                    {priceRanges.map((range) => (
                                        <option key={range.value} value={range.value}>
                                            {range.label}
                                        </option>
                                    ))}
                                </select>
                                <i className="fas fa-chevron-down absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-xs pointer-events-none"></i>
                            </div>
                        </div>
                        {/* Sort Options */}
                        <div>
                            <h3 className="text-sm font-medium text-gray-900 mb-3">정렬</h3>
                            <div className="relative">
                                <select
                                    value={sortBy}
                                    onChange={(e) => setSortBy(e.target.value)}
                                    className="w-full px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer appearance-none"
                                >
                                    {sortOptions.map((option) => (
                                        <option key={option.value} value={option.value}>
                                            {option.label}
                                        </option>
                                    ))}
                                </select>
                                <i className="fas fa-chevron-down absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-xs pointer-events-none"></i>
                            </div>
                        </div>
                    </div>
                </div>
                {/* Results Count */}
                <div className="flex items-center justify-between mb-6">
                    <p className="text-gray-600">
                        총{" "}
                        <span className="font-semibold text-gray-900">
                            {sneakers.length}
                        </span>
                        개의 상품
                    </p>
                    <div className="flex items-center space-x-2">
                        <button className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50 cursor-pointer">
                            <i className="fas fa-th text-gray-600"></i>
                        </button>
                        <button className="p-2 border border-gray-300 rounded-lg hover:bg-gray-50 cursor-pointer">
                            <i className="fas fa-list text-gray-600"></i>
                        </button>
                    </div>
                </div>
                {/* Product Grid */}
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 mb-12">
                    {sneakers.map((sneaker) => (
                        <div
                            key={sneaker.id}
                            className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow overflow-hidden cursor-pointer"
                        >
                            <a
                                href="https://readdy.ai/home/8c14b666-4886-429c-ad07-c16c2cd22c03/ad0cf9eb-0d08-4f31-aba6-a87043edcd2e"
                                data-readdy="true"
                            >
                                <div className="aspect-square overflow-hidden">
                                    <img
                                        src={sneaker.image}
                                        alt={`${sneaker.brand} ${sneaker.model}`}
                                        className="w-full h-full object-cover object-top hover:scale-105 transition-transform duration-300"
                                    />
                                </div>
                                <div className="p-4">
                                    <div className="mb-2">
                                        <p className="text-sm text-gray-500 font-medium">
                                            {sneaker.brand}
                                        </p>
                                        <h3 className="text-lg font-semibold text-gray-900 line-clamp-2">
                                            {sneaker.model}
                                        </h3>
                                    </div>
                                    <div className="flex items-center justify-between mb-2">
                                        <div>
                                            <p className="text-xs text-gray-500">현재가</p>
                                            <p className="text-lg font-bold text-blue-600">
                                                ₩{sneaker.currentBid}
                                            </p>
                                        </div>
                                        <div className="text-right">
                                            <p className="text-xs text-gray-500">참여자</p>
                                            <p className="text-sm font-semibold text-gray-900">
                                                {sneaker.bidders}명
                                            </p>
                                        </div>
                                    </div>
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center space-x-1">
                                            <i className="fas fa-clock text-red-500 text-xs"></i>
                                            <span className="text-sm font-medium text-red-500">
                                                {sneaker.timeLeft}
                                            </span>
                                        </div>
                                        <button className="px-3 py-1 bg-blue-500 text-white text-xs !rounded-button hover:bg-blue-600 cursor-pointer whitespace-nowrap">
                                            입찰하기
                                        </button>
                                    </div>
                                </div>
                            </a>
                        </div>
                    ))}
                </div>
                {/* Pagination */}
                <div className="flex items-center justify-center space-x-2">
                    <button
                        onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
                        disabled={currentPage === 1}
                        className="px-3 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                    >
                        <i className="fas fa-chevron-left"></i>
                    </button>
                    {[...Array(totalPages)].map((_, index) => {
                        const page = index + 1;
                        return (
                            <button
                                key={page}
                                onClick={() => setCurrentPage(page)}
                                className={`px-3 py-2 text-sm rounded-lg cursor-pointer ${currentPage === page
                                    ? "bg-blue-500 text-white"
                                    : "border border-gray-300 text-gray-700 hover:bg-gray-50"
                                    }`}
                            >
                                {page}
                            </button>
                        );
                    })}
                    <button
                        onClick={() =>
                            setCurrentPage(Math.min(totalPages, currentPage + 1))
                        }
                        disabled={currentPage === totalPages}
                        className="px-3 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                    >
                        <i className="fas fa-chevron-right"></i>
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Category;
