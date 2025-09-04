import type { Transaction } from "../features/transaction/types/Transaction";

export const getStatusColor = (status: Transaction['status']): string => {
    switch (status) {
        case 'completed':
            return 'text-green-600 bg-green-50';
        case 'shipping':
            return 'text-blue-600 bg-blue-50';
        case 'cancelled':
            return 'text-red-600 bg-red-50';
        default:
            return 'text-gray-600 bg-gray-50';
    }
};