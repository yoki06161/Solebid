import type { ProfileBidProps } from "../types/ProfileBidProps";
import type { ProfileStatProps } from "../types/ProfileStatProps";
import type { ProfileWishProps } from "../types/ProfileWishProps";

export const menu = [
    { icon: "fas fa-shopping-cart", text: "주문/배송 조회", href: "/order" },
    { icon: 'fas fa-receipt', text: '결제내역 조회', href: '/points/records' },
    { icon: "fas fa-heart", text: "찜한 상품", href: "/wish" },
    { icon: "fas fa-cog", text: "설정", href: "/setting" },
];

export const bidData: ProfileBidProps[] = [
    { id: 1, name: "무선 블루투스 헤드폰", date: "2024.01.15", price: "89,000원", imageUrl: "https://readdy.ai/api/search-image?query=modern%20wireless%20bluetooth%20headphones%20black%20color%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=60&height=60&seq=product001&orientation=squarish" },
    { id: 2, name: "프리미엄 원두 커피", date: "2024.01.12", price: "25,000원", imageUrl: "https://readdy.ai/api/search-image?query=premium%20coffee%20beans%20package%20bag%20dark%20roast%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=60&height=60&seq=product002&orientation=squarish" },
    { id: 3, name: "스마트폰 케이스", date: "2024.01.10", price: "15,000원", imageUrl: "https://readdy.ai/api/search-image?query=modern%20smartphone%20case%20clear%20transparent%20design%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=60&height=60&seq=product003&orientation=squarish" },
];

export const statsData: ProfileStatProps[] = [
    { label: "총 주문", value: 12, color: "text-blue-600" },
    { label: "적립 포인트", value: "2,450", color: "text-purple-600" },
];

export const wishData: ProfileWishProps[] = [
    { id: 1, name: "럭셔리 시계", price: "299,000원", imageUrl: "https://readdy.ai/api/search-image?query=elegant%20watch%20luxury%20timepiece%20silver%20metal%20band%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=120&height=120&seq=wishlist001&orientation=squarish" },
    { id: 2, name: "노트북", price: "1,299,000원", imageUrl: "https://readdy.ai/api/search-image?query=modern%20laptop%20computer%20silver%20aluminum%20design%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=120&height=120&seq=wishlist002&orientation=squarish" },
    { id: 3, name: "스킨케어 세트", price: "89,000원", imageUrl: "https://readdy.ai/api/search-image?query=premium%20skincare%20cream%20jar%20white%20container%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=120&height=120&seq=wishlist003&orientation=squarish" },
];