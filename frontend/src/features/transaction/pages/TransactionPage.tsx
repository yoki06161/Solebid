import { useMemo, useState } from "react";
import { TransactionList, TransactionSearch, TransactionSummary } from "../components";
import { transactionData } from "../components/mockData";
import type { Transaction } from "../types/Transaction";

const TransactionPage = () => {
    const [selectedDateFilter, setSelectedDateFilter] = useState("all");
    const [selectedStatusFilter, setSelectedStatusFilter] = useState("all");
    const [searchQuery, setSearchQuery] = useState("");
    const [showDateDropdown, setShowDateDropdown] = useState(false);
    const [showStatusDropdown, setShowStatusDropdown] = useState(false);

    const filteredData = useMemo(() => {
        const now = new Date();
        const oneWeekAgo = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7);
        const oneMonthAgo = new Date(now.getFullYear(), now.getMonth() - 1, now.getDate());
        const threeMonthsAgo = new Date(now.getFullYear(), now.getMonth() - 3, now.getDate());

        return transactionData.filter((item: Transaction) => {
            const matchesSearch = item.name
                .toLowerCase()
                .includes(searchQuery.toLowerCase());

            const matchesStatus =
                selectedStatusFilter === "all" || item.status === selectedStatusFilter;

            const itemDate = new Date(item.date);

            let matchesDate = true;

            switch (selectedDateFilter) {
                case "week":
                    matchesDate = itemDate >= oneWeekAgo;
                    break;
                case "month":
                    matchesDate = itemDate >= oneMonthAgo;
                    break;
                case "3months":
                    matchesDate = itemDate >= threeMonthsAgo;
                    break;
                default: // "all"
                    matchesDate = true;
                    break;
            }

            return matchesSearch && matchesStatus && matchesDate;
        });
    }, [searchQuery, selectedStatusFilter, selectedDateFilter]);

    return (
        <div className="min-h-screen bg-gray-50">
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <TransactionSearch
                    searchQuery={searchQuery}
                    setSearchQuery={setSearchQuery}
                    showDateDropdown={showDateDropdown}
                    setShowDateDropdown={setShowDateDropdown}
                    setSelectedDateFilter={setSelectedDateFilter}
                    showStatusDropdown={showStatusDropdown}
                    setShowStatusDropdown={setShowStatusDropdown}
                    setSelectedStatusFilter={setSelectedStatusFilter}
                    selectedDateFilter={selectedDateFilter}
                    selectedStatusFilter={selectedStatusFilter}
                />
                <TransactionSummary
                    data={filteredData}
                />
                <TransactionList
                    data={filteredData}
                />
                {filteredData.length > 0 && (
                    <div className="text-center mt-8">
                        <button
                            onClick={() => { }}
                            className="px-6 py-3 bg-white border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 cursor-pointer !rounded-button whitespace-nowrap"
                        >
                            <i className="fas fa-plus mr-2" />
                            더 많은 판매 내역 보기
                        </button>
                    </div>
                )}
            </main>
        </div>
    );
};

export default TransactionPage;