import React from 'react';
import type { Payment } from '../types/payment';

interface PaymentsTableProps {
    payments: Payment[];
    onShowDetail: (payment: Payment) => void;
}

const PaymentsTable: React.FC<PaymentsTableProps> = ({ payments, onShowDetail }) => {
    const statusLabel = (s: Payment['status']) => (s === 'completed' ? '결제완료' : '결제취소');
    const statusClass = (s: Payment['status']) =>
        s === 'completed' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800';

    return (
        <div className="overflow-x-auto">
            <table className="w-full">
                <thead>
                <tr className="bg-gray-50">
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">결제일시</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">결제금액</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">포인트</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">결제수단</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">결제상태</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상세보기</th>
                </tr>
                </thead>

                <tbody className="bg-white divide-y divide-gray-200">
                {payments.map((p) => (
                    <tr key={p.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{p.date}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                            {p.amount.toLocaleString('ko-KR')}원
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-blue-600 font-medium">
                            +{p.convertedPoint.toLocaleString('ko-KR')}P
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{p.method}</td>
                        <td className="px-6 py-4 whitespace-nowrap">
                <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${statusClass(p.status)}`}>
                  {statusLabel(p.status)}
                </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm">
                            <button
                                onClick={() => onShowDetail(p)}
                                className="text-blue-600 hover:text-blue-900 !rounded-button whitespace-nowrap"
                            >
                                상세보기
                            </button>
                        </td>
                    </tr>
                ))}

                {payments.length === 0 && (
                    <tr>
                        <td colSpan={6} className="px-6 py-10 text-center text-sm text-gray-500">
                            조회 결과가 없습니다.
                        </td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
};

export default PaymentsTable;
