import { useEffect, useState } from 'react';
import type { Payment } from '../types/payment.ts'; // 파일명이 Payment.ts면 대소문자 맞추기!
import { fetchPayments } from '../services/paymentService';

export function usePayments(userId: number) { // ← userId를 훅 인자로 받자
    const [payments, setPayments] = useState<Payment[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<Error | null>(null);

    const reload = async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await fetchPayments({
                userId,
                page: 0,               // 서버 페이징(0-based)
                size: 20,
                status: 'ALL',
                sort: 'requestedAt,desc',
            });
            setPayments(res.items);
        } catch (e) {
            setError(e instanceof Error ? e : new Error('Unknown error'));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void reload();
    }, [userId]); // userId 변동시 리로드

    return { payments, setPayments, loading, error, reload };
}
