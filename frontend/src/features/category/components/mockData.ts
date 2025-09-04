export const brands = ["Nike", "Adidas", "New Balance", "Puma", "Converse", "Vans"];

export const priceRanges = [
    { value: "0-100000", label: "10만원 이하" },
    { value: "100000-200000", label: "10-20만원" },
    { value: "200000-300000", label: "20-30만원" },
    { value: "300000-500000", label: "30-50만원" },
    { value: "500000+", label: "50만원 이상" },
];

export const sortOptions = [
    { value: "popular", label: "인기순" },
    { value: "ending", label: "마감임박순" },
    { value: "newest", label: "최신순" },
    { value: "price-low", label: "낮은가격순" },
    { value: "price-high", label: "높은가격순" },
];

export const categories = [
    {
        id: 1,
        image:
            "https://readdy.ai/api/search-image?query=premium%20Nike%20Air%20Jordan%201%20black%20and%20red%20basketball%20sneakers%20on%20clean%20white%20background%20with%20professional%20studio%20lighting%20and%20minimal%20shadows%20for%20product%20photography&width=300&height=300&seq=20&orientation=squarish",
        brand: "Nike",
        model: "Air Jordan 1 Retro High",
        currentBid: 289000,
        timeLeft: "2:15:33",
        bidders: 42,
        price: 850000,
        releaseDate: "2022-11-19",
    },
    {
        id: 2,
        image:
            "https://readdy.ai/api/search-image?query=luxury%20Adidas%20Yeezy%20350%20V2%20cream%20white%20lifestyle%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=21&orientation=squarish",
        brand: "Adidas",
        model: "Yeezy Boost 350 V2",
        currentBid: 459000,
        timeLeft: "1:42:18",
        bidders: 67,
        price: 320000,
        releaseDate: "2020-02-15",
    },
    {
        id: 3,
        image:
            "https://readdy.ai/api/search-image?query=premium%20New%20Balance%20990v5%20gray%20and%20navy%20running%20sneakers%20on%20clean%20white%20background%20with%20studio%20lighting%20and%20minimal%20shadows%20for%20product%20photography&width=300&height=300&seq=22&orientation=squarish",
        brand: "New Balance",
        model: "990v5 Made in USA",
        currentBid: 219000,
        timeLeft: "4:33:07",
        bidders: 28,
        price: 850000,
        releaseDate: "2022-11-19",
    },
    {
        id: 4,
        image:
            "https://readdy.ai/api/search-image?query=trendy%20Nike%20Dunk%20Low%20panda%20black%20and%20white%20skateboarding%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=23&orientation=squarish",
        brand: "Nike",
        model: "Dunk Low Panda",
        currentBid: 189000,
        timeLeft: "3:21:45",
        bidders: 35,
        price: 320000,
        releaseDate: "2020-02-15",
    },
    {
        id: 5,
        image:
            "https://readdy.ai/api/search-image?query=premium%20Adidas%20Stan%20Smith%20white%20and%20green%20tennis%20sneakers%20on%20clean%20white%20background%20with%20studio%20lighting%20and%20minimal%20shadows%20for%20product%20photography&width=300&height=300&seq=24&orientation=squarish",
        brand: "Adidas",
        model: "Stan Smith Original",
        currentBid: 129000,
        timeLeft: "6:18:22",
        bidders: 19,
        price: 850000,
        releaseDate: "2022-11-19",
    },
    {
        id: 6,
        image:
            "https://readdy.ai/api/search-image?query=luxury%20Puma%20Suede%20Classic%20burgundy%20and%20white%20lifestyle%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=25&orientation=squarish",
        brand: "Puma",
        model: "Suede Classic XXI",
        currentBid: 99000,
        timeLeft: "5:44:11",
        bidders: 15,
        price: 320000,
        releaseDate: "2020-02-15",
    },
    {
        id: 7,
        image:
            "https://readdy.ai/api/search-image?query=trendy%20Converse%20Chuck%20Taylor%20All%20Star%20high%20top%20black%20canvas%20sneakers%20on%20clean%20white%20background%20with%20studio%20lighting%20and%20minimal%20shadows&width=300&height=300&seq=26&orientation=squarish",
        brand: "Converse",
        model: "Chuck Taylor All Star High",
        currentBid: 79000,
        timeLeft: "7:12:33",
        bidders: 12,
        price: 850000,
        releaseDate: "2022-11-19",
    },
    {
        id: 8,
        image:
            "https://readdy.ai/api/search-image?query=premium%20Vans%20Old%20Skool%20black%20and%20white%20skateboarding%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=27&orientation=squarish",
        brand: "Vans",
        model: "Old Skool Classic",
        currentBid: 119000,
        timeLeft: "2:55:17",
        bidders: 23,
        price: 320000,
        releaseDate: "2020-02-15",
    },
    {
        id: 9,
        image:
            "https://readdy.ai/api/search-image?query=luxury%20Nike%20Air%20Max%2090%20white%20and%20gray%20running%20sneakers%20on%20clean%20white%20background%20with%20studio%20lighting%20and%20minimal%20shadows%20for%20product%20photography&width=300&height=300&seq=28&orientation=squarish",
        brand: "Nike",
        model: "Air Max 90 Essential",
        currentBid: 169000,
        timeLeft: "1:33:28",
        bidders: 31,
        price: 850000,
        releaseDate: "2022-11-19",
    },
    {
        id: 10,
        image:
            "https://readdy.ai/api/search-image?query=premium%20Adidas%20Ultraboost%2022%20blue%20and%20white%20running%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=29&orientation=squarish",
        brand: "Adidas",
        model: "Ultraboost 22",
        currentBid: 239000,
        timeLeft: "4:07:55",
        bidders: 38,
        price: 320000,
        releaseDate: "2020-02-15",
    },
    {
        id: 11,
        image:
            "https://readdy.ai/api/search-image?query=trendy%20New%20Balance%20327%20orange%20and%20navy%20retro%20sneakers%20on%20clean%20white%20background%20with%20studio%20lighting%20and%20minimal%20shadows%20for%20product%20photography&width=300&height=300&seq=30&orientation=squarish",
        brand: "New Balance",
        model: "327 Retro",
        currentBid: 149000,
        timeLeft: "3:48:12",
        bidders: 26,
        price: 850000,
        releaseDate: "2022-11-19",
    },
    {
        id: 12,
        image:
            "https://readdy.ai/api/search-image?query=luxury%20Puma%20RS-X%20white%20and%20multicolor%20chunky%20sneakers%20on%20clean%20white%20background%20with%20professional%20product%20photography%20and%20minimal%20shadows&width=300&height=300&seq=31&orientation=squarish",
        brand: "Puma",
        model: "RS-X Reinvention",
        currentBid: 179000,
        timeLeft: "6:25:41",
        bidders: 29,
        price: 320000,
        releaseDate: "2020-02-15",
    },
];