import { useMemo } from "react";
import type { TransactionSummaryProps } from "../types/TransactionSummaryProps";

const TransactionSummary = ({ data }: TransactionSummaryProps) => {
    const summary = useMemo(() => {
        const totalCount = data.length;
        const completedCount = data.filter(item => item.status === 'completed').length;
        const shippingCount = data.filter(item => item.status === 'shipping').length;
        const totalAmount = data.reduce((sum, item) => sum + item.price, 0);

        return { totalCount, completedCount, shippingCount, totalAmount };
    }, [data]);

    return (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div className="bg-white rounded-lg shadow-sm p-6 text-center">
                <div className="text-2xl font-bold text-blue-600 mb-1">
                    {summary.totalCount}
                </div>
                <div className="text-gray-600 text-sm">
                    총 판매 건수
                </div>
            </div>
            <div className="bg-white rounded-lg shadow-sm p-6 text-center">
                <div className="text-2xl font-bold text-green-600 mb-1">
                    {summary.completedCount}
                </div>
                <div className="text-gray-600 text-sm">
                    판매 완료
                </div>
            </div>
            <div className="bg-white rounded-lg shadow-sm p-6 text-center">
                <div className="text-2xl font-bold text-blue-600 mb-1">
                    {summary.shippingCount}
                </div>
                <div className="text-gray-600 text-sm">
                    배송중
                </div>
            </div>
            <div className="bg-white rounded-lg shadow-sm p-6 text-center">
                <div className="text-2xl font-bold text-purple-600 mb-1">
                    ₩{summary.totalAmount.toLocaleString()}
                </div>
                <div className="text-gray-600 text-sm">
                    총 판매 금액
                </div>
            </div>
        </div>
    );
};

export default TransactionSummary;