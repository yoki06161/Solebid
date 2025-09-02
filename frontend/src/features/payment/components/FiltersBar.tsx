import React from 'react';
import type { DateFilter, PaymentTableFilter } from '../types/payment';

interface Props {
    dateFilter: DateFilter;
    setDateFilter: React.Dispatch<React.SetStateAction<DateFilter>>;
    searchTerm: string;
    setSearchTerm: React.Dispatch<React.SetStateAction<string>>;
    paymentStatus: PaymentTableFilter;
    setPaymentStatus: React.Dispatch<React.SetStateAction<PaymentTableFilter>>;
}

const FiltersBar: React.FC<Props> = ({
                                         dateFilter, setDateFilter, searchTerm, setSearchTerm, paymentStatus, setPaymentStatus,
                                     }) => {
    return (
        <div className="flex flex-wrap gap-4 mb-6">
            {/* 기간 */}
            <div className="flex-1 min-w-[300px]">
                <div className="flex space-x-2">
                    {([
                        { key: 'today', label: '오늘' },
                        { key: '1week', label: '1주일' },
                        { key: '1month', label: '1개월' },
                        { key: '3months', label: '3개월' },
                    ] as { key: DateFilter; label: string }[]).map(({ key, label }) => (
                        <button
                            key={key}
                            onClick={() => setDateFilter(key)}
                            className={`px-4 py-2 text-sm rounded-lg ${
                                dateFilter === key ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700'
                            } !rounded-button whitespace-nowrap`}
                        >
                            {label}
                        </button>
                    ))}
                </div>
            </div>

            {/* 검색 */}
            <div className="flex-1 min-w-[300px]">
                <div className="relative">
                    <input
                        type="text"
                        placeholder="결제수단 검색"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full pl-10 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                    />
                    <i className="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                </div>
            </div>

            {/* 상태 */}
            <div className="flex items-center space-x-2">
                {([
                    { key: 'all', label: '전체' },
                    { key: 'completed', label: '결제완료' },
                    { key: 'cancelled', label: '결제취소' },
                ] as { key: PaymentTableFilter; label: string }[]).map(({ key, label }) => (
                    <button
                        key={key}
                        onClick={() => setPaymentStatus(key)}
                        className={`px-4 py-2 text-sm rounded-lg ${
                            paymentStatus === key ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700'
                        } !rounded-button whitespace-nowrap`}
                    >
                        {label}
                    </button>
                ))}
            </div>
        </div>
    );
};

export default FiltersBar;
