// 서버 DTO
export type ServerPaymentStatus = 'SUCCESS' | 'CANCELLED' | 'FAILED' | 'PENDING';

export interface ServerPayment {
    paymentId: number;
    userId: number;
    orderId: string;
    transactionId: string;
    amount: number;
    paymentMethod: string;  // 'CARD' | ...
    provider: string;       // 'PORTONE' | ...
    paymentStatus: ServerPaymentStatus;
    charged: boolean;
    convertedPoint: number;
    requestedAt: string;    // '2025-08-30T20:16:24.843663'
    confirmedAt?: string | null;
}

export interface PaymentsPageDto {
    page: number;           // 0-based
    content: ServerPayment[];
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

export type UiStatus = 'completed' | 'cancelled';

export interface Payment {
    id: number;
    userId: number;
    orderId: string;
    transactionId: string;
    amount: number;          // 금액(원)
    convertedPoint: number;  // 전환 포인트
    method: string;          // '신용카드' 등 한글 표기
    status: UiStatus;        // 배지 색상용: 완료/취소
    date: string;            // 테이블 표시 날짜 'YYYY-MM-DD'
    requestedAt: string;
    confirmedAt?: string;
}

export interface FetchPaymentsParams {
    userId: number;
    page?: number;           // 0-based
    size?: number;
    status?: ServerPaymentStatus | 'ALL';
    from?: string;           // 'YYYY-MM-DD'
    to?: string;             // 'YYYY-MM-DD'
    sort?: string;           // 'requestedAt,desc' 등
}
