import { useEffect, useState } from 'react';
import type { Payment, ServerPayment, ServerPaymentStatus } from '../types/payment.ts';
import { fetchPayments } from '../services/paymentService';

function mapMethodToKo(code: string) {
    switch (code) {
        case 'CARD': return '신용카드';
        case 'KAKAO_PAY': return '카카오페이';
        case 'TRANSFER': return '계좌이체';
        default: return code;
    }
}

function mapStatusToUi(status: ServerPaymentStatus): 'completed' | 'cancelled' {
    if (status === 'SUCCESS') return 'completed';
    return 'cancelled';
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

function mapServerPayment(payment: ServerPayment): Payment {
    const baseDate = payment.confirmedAt || payment.requestedAt;
    return {
        id: payment.paymentId,
        userId: payment.userId,
        orderId: payment.orderId,
        transactionId: payment.transactionId,
        amount: payment.amount,
        convertedPoint: payment.convertedPoint,
        method: mapMethodToKo(payment.paymentMethod),
        status: mapStatusToUi(payment.paymentStatus),
        date: toYmd(baseDate),
        requestedAt: payment.requestedAt,
        confirmedAt: payment.confirmedAt ?? null,
    };
}

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
            const mapped = (res.content ?? []).map(mapServerPayment);
            setPayments(mapped);
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
