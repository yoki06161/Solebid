// 서버 상태값
export type PaymentStatus = 'SUCCESS' | 'CANCELLED' | 'FAILED' | 'PENDING';
export type ServerPaymentStatus = PaymentStatus;

// 서버 호출 파라미터
export interface FetchPaymentsParams {
    userId: number;  // 필수
    page?: number;   // 0-based
    size?: number;
    status?: PaymentStatus | 'ALL';
    from?: string;   // 'YYYY-MM-DD'
    to?: string;     // 'YYYY-MM-DD'
    sort?: string;   // 'requestedAt,desc'
}

// 서버 응답 아이템
export interface ServerPayment {
    paymentId: number;
    userId: number;
    orderId: string;
    transactionId: string;
    amount: number;
    paymentMethod: string;
    provider: string;
    paymentStatus: PaymentStatus;
    charged: boolean;
    convertedPoint: number;
    requestedAt: string;
    confirmedAt?: string | null;
}

// 페이지 DTO
export interface PaymentsPageDto {
    page: number;
    content: ServerPayment[];
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

// UI 모델
export type UiStatus = 'completed' | 'cancelled';
export interface Payment {
    id: number;
    userId: number;
    orderId: string;
    transactionId: string;
    amount: number;
    convertedPoint: number;
    method: string;
    status: UiStatus;
    date: string;            // YYYY-MM-DD
    requestedAt: string;     // ISO
    confirmedAt?: string | null;
}

// 필터 타입
export type DateFilter = 'today' | '1week' | '1month' | '3months';
export type PaymentTableFilter = 'all' | 'completed' | 'cancelled';
