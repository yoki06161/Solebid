import { useMemo, useState } from "react";
import Pagination from "../../../components/Pagination";
import { getFromDate } from "../../../utils/get-from-date";
import { OrderList, OrderSearch } from "../components";
import { orders as mockOrders, periods, statuses } from "../components/mockData";

const ITEMS_PER_PAGE = 3;

const OrderPage = () => {
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedPeriod, setSelectedPeriod] = useState("전체");
    const [selectedStatus, setSelectedStatus] = useState("전체");
    const [expandedOrder, setExpandedOrder] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState(1);

    const toggleOrderExpansion = (orderId: string) => {
        setExpandedOrder(expandedOrder === orderId ? null : orderId);
    };

    const filteredOrders = useMemo(() => {
        const fromDate = getFromDate(selectedPeriod);
        const today = new Date();

        return mockOrders.filter((order) => {
            const matchesSearch =
                order.id.includes(searchQuery) ||
                order.items.some((item) => item.name.includes(searchQuery));
            const matchesStatus =
                selectedStatus === "전체" || order.status === selectedStatus;

            if (!fromDate) {
                return matchesSearch && matchesStatus;
            }

            const orderDate = new Date(order.date.replace(/\./g, "-"));
            const matchesPeriod = orderDate >= fromDate && orderDate <= today;

            return matchesSearch && matchesStatus && matchesPeriod;
        });
    }, [searchQuery, selectedPeriod, selectedStatus]);

    const totalPages = Math.ceil(filteredOrders.length / ITEMS_PER_PAGE);

    const paginatedOrders = filteredOrders.slice(
        (currentPage - 1) * ITEMS_PER_PAGE,
        currentPage * ITEMS_PER_PAGE
    );

    const handlePageChange = (page: number) => {
        if (page > 0 && page <= totalPages) {
            setCurrentPage(page);
            setExpandedOrder(null);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
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
                    expandedOrder={expandedOrder}
                    toggleOrderExpansion={toggleOrderExpansion}
                />
                {totalPages > 1 && (
                    <Pagination
                        currentPage={currentPage}
                        totalPages={totalPages}
                        onPageChange={handlePageChange}
                    />
                )}
            </div>
        </div>
    );
};

export default OrderPage;
