import { useState } from 'react';
import type { Payment } from '../types/payment';

export function usePaymentDetailModal() {
    const [visible, setVisible] = useState<boolean>(false);
    const [selected, setSelected] = useState<Payment | null>(null);

    const open = (p: Payment) => {
        setSelected(p);
        setVisible(true);
    };
    const close = () => setVisible(false);

    return { visible, selected, open, close };
}
