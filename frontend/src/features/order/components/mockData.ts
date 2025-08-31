import type { Order } from "../types/Order";

export const periods = [
    "전체",
    "최근 1개월",
    "최근 3개월",
    "최근 6개월",
    "최근 1년",
];

export const statuses = [
    "전체",
    "결제완료",
    "배송준비중",
    "배송중",
    "배송완료",
    "취소·반품",
];

export const orders: Order[] = [
    {
        id: "ORD-2024-001234",
        date: "2024.01.15",
        items: [
            {
                name: "무선 블루투스 헤드폰",
                image:
                    "https://readdy.ai/api/search-image?query=modern%20wireless%20bluetooth%20headphones%20black%20color%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=80&height=80&seq=order001&orientation=squarish",
                price: 89000,
                options: "블랙, 노이즈캔슬링",
            },
        ],
        totalAmount: 89000,
        status: "배송완료",
        statusColor: "green",
        trackingNumber: "CJ1234567890",
        deliveryAddress: "서울특별시 강남구 테헤란로 123 ABC빌딩 456호",
        payment: {
            method: "신용카드",
            cardInfo: "KB국민카드 ****-****-****-1234",
            status: "결제완료",
            itemAmount: 89000,
            shippingFee: 0,
            discount: 0,
            finalAmount: 89000,
        },
        shipping: {
            recipient: "김철수",
            phone: "010-1234-5678",
            address: "서울특별시 강남구 테헤란로 123",
            addressDetail: "ABC빌딩 456호",
            zipCode: "06142",
            request: "부재시 경비실에 맡겨주세요",
            trackingNumber: "CJ1234567890",
            courier: "CJ대한통운",
        },
        timeline: [
            { status: "주문접수", date: "2024.01.15 14:32", completed: true },
            { status: "결제완료", date: "2024.01.15 14:33", completed: true },
            { status: "상품준비중", date: "2024.01.15 16:20", completed: true },
            { status: "배송시작", date: "2024.01.16 09:15", completed: true },
            { status: "배송완료", date: "2024.01.17 11:30", completed: true },
        ],
    },
    {
        id: "ORD-2024-001233",
        date: "2024.01.12",
        items: [
            {
                name: "프리미엄 원두 커피",
                image:
                    "https://readdy.ai/api/search-image?query=premium%20coffee%20beans%20package%20bag%20dark%20roast%20clean%20white%20background%20product%20photography%20professional%20lighting%20high%20quality&width=80&height=80&seq=order002&orientation=squarish",
                price: 25000,
            },
        ],
        totalAmount: 25000,
        status: "배송중",
        statusColor: "blue",
        trackingNumber: "CJ0987654321",
        deliveryAddress: "서울특별시 강남구 테헤란로 123 ABC빌딩 456호",
        payment: {
            method: "카카오페이",
            status: "결제완료",
            itemAmount: 25000,
            shippingFee: 3000,
            discount: 0,
            finalAmount: 28000,
        },
        shipping: {
            recipient: "이영희",
            phone: "010-9876-5432",
            address: "서울특별시 서초구 서초대로 456",
            addressDetail: "DEF 오피스텔 789호",
            zipCode: "06610",
            request: "문 앞에 놓아주세요.",
            trackingNumber: "CJ0987654321",
            courier: "CJ대한통운",
        },
        timeline: [
            { status: "주문접수", date: "2024.01.12 10:00", completed: true },
            { status: "결제완료", date: "2024.01.12 10:01", completed: true },
            { status: "상품준비중", date: "2024.01.12 11:30", completed: true },
            { status: "배송시작", date: "2024.01.13 14:00", completed: true },
            { status: "배송중", date: "2024.01.14 09:00", completed: true },
            { status: "배송완료", date: "", completed: false },
        ],
    },
];