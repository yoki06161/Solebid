import { useMemo, useState } from 'react';
import type { DateFilter, Payment, PaymentTableFilter } from '../types/payment';

const DAY_MS = 24 * 60 * 60 * 1000;

function parseYMD(dateStr: string): Date {
    const [y, m, d] = dateStr.split('-').map(Number);
    return new Date(y, (m ?? 1) - 1, d ?? 1);
}

function rangeFor(filter: DateFilter) {
    const end = new Date();
    end.setHours(23, 59, 59, 999);
    const start = new Date(end);
    switch (filter) {
        case 'today':
            start.setHours(0, 0, 0, 0);
            break;
        case '1week':
            start.setTime(end.getTime() - 7 * DAY_MS);
            break;
        case '1month':
            start.setTime(end.getTime() - 30 * DAY_MS);
            break;
        case '3months':
            start.setTime(end.getTime() - 90 * DAY_MS);
            break;
    }
    return { start, end };
}

export function usePaymentFilters(payments: Payment[]) {
    const [dateFilter, setDateFilter] = useState<DateFilter>('1month');
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [paymentStatus, setPaymentStatus] = useState<PaymentTableFilter>('all');

    const filtered = useMemo(() => {
        const { start, end } = rangeFor(dateFilter);
        const q = searchTerm.trim().toLowerCase();

        return payments
            .filter((p) => {
                const d = parseYMD(p.date);
                if (d < start || d > end) return false;
                if (paymentStatus !== 'all' && p.status !== paymentStatus) return false;
                if (q && !p.method.toLowerCase().includes(q)) return false;
                return true;
            })
            .sort((a, b) => (a.date < b.date ? 1 : -1));
    }, [payments, dateFilter, searchTerm, paymentStatus]);

    return {
        dateFilter,
        setDateFilter,
        searchTerm,
        setSearchTerm,
        paymentStatus,
        setPaymentStatus,
        filtered,
    };
}
