import { useMemo, useState } from "react";
import Pagination from "../../../components/Pagination";
import { usePagination } from "../../../hooks/usePagination";
import { getFromDate } from "../../../utils/get-from-date";
import { OrderList, OrderSearch } from "../components";
import { orders, periods, statuses } from "../components/mockData";
import type { Order } from "../types/Order";

const OrderPage = () => {
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedPeriod, setSelectedPeriod] = useState("전체");
    const [selectedStatus, setSelectedStatus] = useState("전체");

    const filteredOrders = useMemo(() => {
        const fromDate = getFromDate(selectedPeriod);
        const today = new Date();

        const matchesSearch = (order: Order): boolean =>
            order.id.includes(searchQuery) ||
            order.items.some((item) => item.name.includes(searchQuery));

        const matchesStatus = (order: Order): boolean =>
            selectedStatus === "전체" || order.status === selectedStatus;

        const matchesPeriod = (order: Order): boolean => {
            if (!fromDate) {
                return true;
            }
            const orderDate = new Date(order.date.replace(/\./g, "-"));
            return orderDate >= fromDate && orderDate <= today;
        };

        return orders.filter(
            (order) => matchesSearch(order) && matchesStatus(order) && matchesPeriod(order)
        );
    }, [searchQuery, selectedPeriod, selectedStatus]);

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
