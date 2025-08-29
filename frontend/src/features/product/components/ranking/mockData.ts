export const productRankings = [
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

export const sellerRankings = [
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

export const bidderRankings = [
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

export const tabs: { id: string; label: string }[] = [
    { id: "products", label: "상품 랭킹" },
    { id: "sellers", label: "판매자 랭킹" },
    { id: "bidders", label: "입찰자 랭킹" },
];