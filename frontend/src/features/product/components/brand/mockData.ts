import type { Brand, BrandWithProducts } from "../../types/brand/Brand";

export const popularBrands: Brand[] = [
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

export const brandProducts: BrandWithProducts[] = [
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