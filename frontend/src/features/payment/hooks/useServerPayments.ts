// hooks/useServerPayments.ts
import { useEffect, useState } from 'react';
import type {
    FetchPaymentsParams,
    Payment,
    PaymentsPageDto,
    ServerPayment,
    ServerPaymentStatus,
} from '../types/payment';
import { fetchPayments } from '../services/paymentService';

// ---- 매핑 유틸 (간단 버전) ----
function mapMethodToKo(code: string) {
    switch (code) {
        case 'CARD': return '신용카드';
        case 'KAKAO_PAY': return '카카오페이';
        case 'TRANSFER': return '계좌이체';
        default: return code;
    }
}
function mapStatusToUi(s: ServerPaymentStatus): 'completed' | 'cancelled' {
    if (s === 'SUCCESS') return 'completed';
    return 'cancelled'; // FAIL/WAITING 표시는 현재 UI에서 '취소'로 통일
}
function toYmd(isoOrYmd?: string | null) {
    if (!isoOrYmd) return '';
    const d = new Date(isoOrYmd);
    if (!Number.isNaN(d.getTime())) {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${y}-${m}-${day}`;
    }
    return isoOrYmd.slice(0, 10);
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

// UI 필터값 → 서버 상태값 변환 (그대로 유지)
export function toServerStatus(ui: 'all' | 'completed' | 'cancelled'): ServerPaymentStatus | 'ALL' {
    if (ui === 'all') return 'ALL';
    return ui === 'completed' ? 'SUCCESS' : 'FAIL';
}

export function useServerPayments(initial: FetchPaymentsParams) {
    const [params, setParams] = useState<FetchPaymentsParams>(initial);
    const [items, setItems] = useState<Payment[]>([]); // 기본값 []
    const [page, setPage] = useState<number>(initial.page ?? 0);
    const [size, setSize] = useState<number>(initial.size ?? 10);
    const [totalPages, setTotalPages] = useState<number>(1);
    const [totalElements, setTotalElements] = useState<number>(0);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<Error | null>(null);

    const reload = async (override?: Partial<FetchPaymentsParams>) => {
        setLoading(true);
        setError(null);
        try {
            const merged = { ...params, ...override, page, size };
            const dto: PaymentsPageDto = await fetchPayments(merged);
            const mapped = (dto.content ?? []).map(mapServerPayment);
            setItems(mapped);
            setTotalPages(dto.totalPages);
            setTotalElements(dto.totalElements);
        } catch (e) {
            setError(e instanceof Error ? e : new Error('Unknown error'));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void reload();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [page, size, params.status, params.from, params.to, params.sort]);

    return {
        items, loading, error,
        page, size, totalPages, totalElements,
        setPage, setSize, setParams, reload,
    };
}
