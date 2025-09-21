export interface MenuDataItem {
    icon: string;
    text: string;
    href: string;
    action?: string;
}

export const menuData: MenuDataItem[] = [
    { icon: "fas fa-shopping-cart", text: "주문/배송 조회", href: "/order" },
    { icon: 'fas fa-receipt', text: '결제내역 조회', href: '/points/records' },
    { icon: "fas fa-user-edit", text: "프로필 편집", href: "#", action: "profile-edit" },
    { icon: "fas fa-shield-alt", text: "민감 정보 편집", href: "#", action: "sensitive-edit" },
    { icon: "fas fa-key", text: "비밀번호 변경", href: "#", action: "password-change" },
];
