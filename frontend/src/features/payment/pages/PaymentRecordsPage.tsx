import React, { useEffect, useMemo, useState } from 'react';
import FiltersBar from '../components/FiltersBar';
import PaymentsTable from '../components/PaymentsTable';
import DetailModal from '../components/DetailModal';
import ServerPagination from '../components/ServerPagination';
import type { DateFilter, Payment, PaymentTableFilter } from '../types/payment';
import { useServerPayments, toServerStatus } from '../hooks/useServerPayments';

// 로컬 타임존 기준 YYYY-MM-DD 범위 계산
function formatYMDLocal(d: Date) {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
}

function rangeFor(filter: DateFilter) {
    const end = new Date();
    end.setHours(23, 59, 59, 999);
    const start = new Date(end);
    const DAY = 24 * 60 * 60 * 1000;

    switch (filter) {
        case 'today':  start.setHours(0, 0, 0, 0); break;
        case '1week':  start.setTime(end.getTime() - 7 * DAY); break;
        case '1month': start.setTime(end.getTime() - 30 * DAY); break;
        case '3months':start.setTime(end.getTime() - 90 * DAY); break;
    }
    return { from: formatYMDLocal(start), to: formatYMDLocal(end) };
}

const PaymentRecordsPage: React.FC = () => {
    // 페이지 상태
    const [dateFilter, setDateFilter] = useState<DateFilter>('1month');
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [paymentStatus, setPaymentStatus] = useState<PaymentTableFilter>('all');

    const { from, to } = rangeFor(dateFilter);

    const {
        items, loading, error, page, totalPages, setPage, setParams,
    } = useServerPayments({
        page: 0,
        size: 10,
        status: 'ALL',
        from,
        to,
        sort: 'requestedAt,desc',
    });

    // 필터 변경 시 서버 파라미터 갱신 + 첫 페이지로 이동
    useEffect(() => {
        setParams((p) => ({
            ...p,
            status: toServerStatus(paymentStatus),
            from,
            to,
            sort: 'requestedAt,desc',
        }));
        setPage(0);
    }, [paymentStatus, from, to, setParams, setPage]);

    // 검색어는 클라이언트에서 결제수단(method)에만 적용
    const visible = useMemo(
        () =>
            (items ?? []).filter((p) =>
                searchTerm.trim()
                    ? p.method.toLowerCase().includes(searchTerm.trim().toLowerCase())
                    : true
            ),
        [items, searchTerm],
    );

    // 상세 모달
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [selectedPayment, setSelectedPayment] = useState<Payment | null>(null);
    const openDetail = (p: Payment) => { setSelectedPayment(p); setShowDetailModal(true); };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* 헤더 */}
            <header className="bg-white shadow-sm border-b">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center">
                            <a href="#" className="text-gray-600 hover:text-gray-900">
                                <i className="fas fa-arrow-left mr-2" />
                                뒤로가기
                            </a>
                            <h1 className="text-2xl font-bold text-gray-900 ml-6">결제내역 조회</h1>
                        </div>
                    </div>
                </div>
            </header>

            {/* 본문 */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="bg-white rounded-lg shadow-sm p-6">
                    <FiltersBar
                        dateFilter={dateFilter}
                        setDateFilter={setDateFilter}
                        searchTerm={searchTerm}
                        setSearchTerm={setSearchTerm}
                        paymentStatus={paymentStatus}
                        setPaymentStatus={setPaymentStatus}
                    />

                    {loading && <div className="py-10 text-center text-sm text-gray-500">불러오는 중…</div>}
                    {error && (
                        <div className="py-10 text-center text-sm text-red-600">
                            데이터를 불러오지 못했습니다: {error.message}
                        </div>
                    )}

                    {!loading && !error && (
                        <>
                            <PaymentsTable payments={visible} onShowDetail={openDetail} />

                            <ServerPagination
                                page0={page}
                                totalPages={totalPages}
                                onPrev={() => setPage(Math.max(0, page - 1))}
                                onNext={() => setPage(page + 1)}
                            />
                        </>
                    )}
                </div>
            </main>

            {showDetailModal && selectedPayment && (
                <DetailModal payment={selectedPayment} onClose={() => setShowDetailModal(false)} />
            )}
        </div>
    );
};

export default PaymentRecordsPage;
