import type { ProfileTransactionProps } from "../types/ProfileTransactionProps";

export const menuData = [
    { icon: "fas fa-shopping-cart", text: "주문/배송 조회", href: "/order" },
    { icon: 'fas fa-receipt', text: '결제내역 조회', href: '/points/records' },
    { icon: "fas fa-cog", text: "설정", href: "/setting" },
];

export const transactionData: ProfileTransactionProps[] = [
    { id: 1, name: "럭셔리 시계", date: "2024.01.15", price: "299,000원", imageUrl: "https://readdy.ai/api/search-image?query=elegant%20watch%20luxury%20timepiece%20silver%20metal%20band%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=120&height=120&seq=wishlist001&orientation=squarish" },
    { id: 2, name: "노트북", date: "2024.01.12", price: "1,299,000원", imageUrl: "https://readdy.ai/api/search-image?query=modern%20laptop%20computer%20silver%20aluminum%20design%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=120&height=120&seq=wishlist002&orientation=squarish" },
    { id: 3, name: "스킨케어 세트", date: "2024.01.10", price: "89,000원", imageUrl: "https://readdy.ai/api/search-image?query=premium%20skincare%20cream%20jar%20white%20container%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=120&height=120&seq=wishlist003&orientation=squarish" },
];