import { useEffect, useState } from 'react';
import type { FetchPaymentsParams, Payment, ServerPaymentStatus } from '../types/payment';
import { fetchPayments } from '../services/paymentService';

export function useServerPayments(initial: FetchPaymentsParams) {
    const [params, setParams] = useState<FetchPaymentsParams>(initial);
    const [items, setItems] = useState<Payment[]>([]);
    const [page, setPage] = useState<number>(initial.page ?? 0); // 0-based
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
            const res = await fetchPayments(merged);
            setItems(res.items);
            setTotalPages(res.totalPages);
            setTotalElements(res.totalElements);
        } catch (e) {
            setError(e instanceof Error ? e : new Error('Unknown error'));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void reload();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [page, size, params.userId, params.status, params.from, params.to, params.sort]);

    return {
        items, loading, error,
        page, size, totalPages, totalElements,
        setPage, setSize, setParams, reload,
    };
}

// UI 필터값 → 서버 상태값 변환
export function toServerStatus(ui: 'all' | 'completed' | 'cancelled'): ServerPaymentStatus | 'ALL' {
    if (ui === 'all') return 'ALL';
    return ui === 'completed' ? 'SUCCESS' : 'CANCELLED';
}
