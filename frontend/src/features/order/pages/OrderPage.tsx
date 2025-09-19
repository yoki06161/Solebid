import { useMemo, useState } from "react";
import Pagination from "../../../components/Pagination";
import { usePagination } from "../../../hooks/usePagination";
import { getFromDate, isDateInRange, parseOrderDate, type Period } from "../../../utils/date-utils";
import { OrderList, OrderSearch } from "../components";
import { periods, statuses } from "../components/mockData";
import { useWinningOrders } from "../hooks/useOrders";
import type { Order } from "../types/Order";

const OrderPage = () => {
    const { data: ordersData, isLoading, isError } = useWinningOrders();

    const [searchQuery, setSearchQuery] = useState("");
    const [selectedPeriod, setSelectedPeriod] = useState("전체");
    const [selectedStatus, setSelectedStatus] = useState("전체");

    const filteredOrders = useMemo(() => {
        if (!ordersData || !Array.isArray(ordersData)) {
            return [];
        }

        const fromDate = getFromDate(selectedPeriod as Period);
        const today = new Date();

        const matchesSearch = (order: Order): boolean =>
            String(order.id).includes(searchQuery) ||
            (order.items || []).some((item) => item.name?.includes(searchQuery));

        const matchesStatus = (order: Order): boolean =>
            selectedStatus === "전체" || order.status === selectedStatus;

        const matchesPeriod = (order: Order): boolean => {
            // "전체" 선택 시 모든 주문 표시
            if (selectedPeriod === "전체" || !fromDate) {
                return true;
            }

            // 주문 날짜가 없으면 제외
            if (!order.date) {
                return false;
            }

            // 날짜 파싱
            const orderDate = parseOrderDate(order.date);
            if (!orderDate) {
                return false;
            }

            return isDateInRange(orderDate, fromDate, today);
        };

        return ordersData.filter(
            (order: Order) => matchesSearch(order) && matchesStatus(order) && matchesPeriod(order)
        );
    }, [searchQuery, selectedPeriod, selectedStatus, ordersData]);

    const {
        paginatedData: paginatedOrders,
        currentPage,
        setCurrentPage,
        totalPages
    } = usePagination({ data: filteredOrders, itemsPerPage: 3 });

    const handlePageChange = (page: number) => {
        if (page > 0 && page <= totalPages) {
            setCurrentPage(page);
        }
    };

    if (isLoading) {
        return (
            <div className="fixed top-0 left-0 w-full h-full flex justify-center items-center">
                <i className="fas fa-spinner fa-spin fa-3x"></i>
            </div>
        );
    }

    if (isError) {
        return (
            <div className="text-center py-10">
                데이터를 불러오는 중 오류가 발생했습니다.
            </div>
        );
    }

    return (
        <main className="min-h-screen bg-gray-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <OrderSearch
                    searchQuery={searchQuery}
                    setSearchQuery={setSearchQuery}
                    periods={periods}
                    selectedPeriod={selectedPeriod}
                    setSelectedPeriod={setSelectedPeriod}
                    statuses={statuses}
                    selectedStatus={selectedStatus}
                    setSelectedStatus={setSelectedStatus}
                />
                <OrderList
                    orders={paginatedOrders}
                />
                {filteredOrders.length > 0 && (
                    <Pagination
                        currentPage={currentPage}
                        totalPages={totalPages}
                        onPageChange={handlePageChange}
                    />
                )}
            </div>
        </main>
    );
};

export default OrderPage;
