import React from 'react';
import type { Payment } from '../types/payment';

interface DetailModalProps {
    payment: Payment;
    onClose: () => void;
}

const fmtDateTime = (s?: string | null) =>
    s ? s.replace('T', ' ').slice(0, 16) : '-';

const DetailModal: React.FC<DetailModalProps> = ({ payment, onClose }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 max-w-2xl w-full mx-4">
                <div className="flex justify-between items-center mb-6">
                    <h3 className="text-lg font-semibold text-gray-900">결제 상세 정보</h3>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
                        <i className="fas fa-times" />
                    </button>
                </div>

                <div className="space-y-6">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <div className="text-sm font-medium text-gray-500">결제일시</div>
                            <div className="mt-1 text-sm text-gray-900">
                                {fmtDateTime(payment.confirmedAt ?? payment.requestedAt)}
                            </div>
                        </div>
                        <div>
                            <div className="text-sm font-medium text-gray-500">결제금액</div>
                            <div className="mt-1 text-sm text-gray-900">{payment.amount.toLocaleString('ko-KR')}원</div>
                        </div>
                        <div>
                            <div className="text-sm font-medium text-gray-500">전환된 포인트</div>
                            <div className="mt-1 text-sm font-medium text-blue-600">
                                +{payment.convertedPoint.toLocaleString('ko-KR')}P
                            </div>
                        </div>
                        <div>
                            <div className="text-sm font-medium text-gray-500">결제수단</div>
                            <div className="mt-1 text-sm text-gray-900">{payment.method}</div>
                        </div>
                        <div>
                            <div className="text-sm font-medium text-gray-500">주문번호</div>
                            <div className="mt-1 text-sm text-gray-900">{payment.orderId}</div>
                        </div>
                        <div>
                            <div className="text-sm font-medium text-gray-500">거래 ID</div>
                            <div className="mt-1 text-sm text-gray-900">{payment.transactionId}</div>
                        </div>
                    </div>

                    <div className="border-t pt-6">
                        <button className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 !rounded-button whitespace-nowrap">
                            <i className="fas fa-download mr-2" />
                            영수증 다운로드
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DetailModal;
