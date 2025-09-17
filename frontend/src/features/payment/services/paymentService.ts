
/*
import type {
    FetchPaymentsParams,
    PaymentsPageDto,
    ServerPayment,
    ServerPaymentStatus,
    Payment,
} from '../types/payment';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

function formatYMDLocal(d: Date) {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
}

function toYmd(isoOrYmd?: string | null) {
    if (!isoOrYmd) return '';
    const d = new Date(isoOrYmd);
    if (!Number.isNaN(d.getTime())) return formatYMDLocal(d);
    return isoOrYmd.slice(0, 10);
}

function mapMethodToKo(code: string) {
    switch (code) {
        case 'CARD': return '신용카드';
        case 'KAKAO_PAY': return '카카오페이';
        case 'TRANSFER': return '계좌이체';
        default: return code;
    }
}

function mapStatusToUi(s: ServerPaymentStatus): 'completed' | 'cancelled' {
    return s === 'SUCCESS' ? 'completed' : 'cancelled';
}

function mapServerPayment(p: ServerPayment): Payment {
    const baseDate = p.confirmedAt || p.requestedAt;
    return {
        id: p.paymentId,
        userId: p.userId,
        orderId: p.orderId,
        transactionId: p.transactionId,
        amount: p.amount,
        convertedPoint: p.convertedPoint,
        method: mapMethodToKo(p.paymentMethod),
        status: mapStatusToUi(p.paymentStatus),
        date: toYmd(baseDate),
        requestedAt: p.requestedAt,
        confirmedAt: p.confirmedAt ?? null,
    };
}

export async function finalizePortoneCharge(payload: {
    impUid: string;
    merchantUid: string;
    amount: number;
}) {
    const res = await fetch('/api/payments/portone/complete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        credentials: 'include',
    });
    if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error(msg || '결제 검증/적립에 실패했습니다.');
    }
    return res.json(); // { addedPoint, currentPoint, ... }
}

export async function fetchPayments(params: FetchPaymentsParams) {
    const search = new URLSearchParams();


    if (params.page != null) search.set('page', String(params.page)); // 0-based
    if (params.size != null) search.set('size', String(params.size));
    if (params.status && params.status !== 'ALL') search.set('status', params.status);
    if (params.from) search.set('from', params.from);
    if (params.to) search.set('to', params.to);
    if (params.sort) search.set('sort', params.sort);

    const res = await fetch(`${BASE_URL}/api/payments/records?${search.toString()}`, {
        method: 'GET',
        headers: { Accept: 'application/json' },
        credentials: 'include',
    });

    if (!res.ok) {
        const t = await res.text().catch(() => '');
        throw new Error(`Payments fetch failed: ${res.status} ${t}`);
    }

    const json = (await res.json()) as PaymentsPageDto;
    const items = (json.content ?? []).map(mapServerPayment);

    return {
        items,
        page: json.page,
        size: json.size,
        totalElements: json.totalElements,
        totalPages: json.totalPages,
        first: json.first,
        last: json.last,
    };
}
*/

// services/paymentService.ts
import { apiFetch } from '../../../utils/apiFetch';
import type { PaymentsPageDto, FetchPaymentsParams } from '../types/payment';

export async function fetchPayments(params: FetchPaymentsParams) {
    const qs = new URLSearchParams();

    if (params.page != null) qs.set('page', String(params.page));
    if (params.size != null) qs.set('size', String(params.size));
    if (params.status && params.status !== 'ALL') qs.set('status', params.status);
    if (params.from) qs.set('from', params.from);
    if (params.to) qs.set('to', params.to);
    if (params.sort) qs.set('sort', params.sort);

    return apiFetch<PaymentsPageDto>(`/api/payments/records?${qs.toString()}`);
}
