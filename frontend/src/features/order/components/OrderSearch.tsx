import type { OrderSearchProps } from "../types/OrderSearch";

const OrderSearch = ({
    searchQuery,
    setSearchQuery,
    periods,
    selectedPeriod,
    setSelectedPeriod,
    statuses,
    selectedStatus,
    setSelectedStatus,
}: OrderSearchProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <div className="space-y-4">
                {/* Search Bar */}
                <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <i className="fas fa-search text-gray-400 text-sm" />
                    </div>
                    <input
                        type="text"
                        placeholder="주문번호 또는 상품명으로 검색"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                    />
                </div>
                {/* Period Filter */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        주문 기간
                    </label>
                    <div className="flex flex-wrap gap-2">
                        {periods.map((period) => (
                            <button
                                key={period}
                                onClick={() => setSelectedPeriod(period)}
                                className={
                                    `px-4 py-2 rounded-lg text-sm font-medium cursor-pointer !rounded-button whitespace-nowrap
                                     ${selectedPeriod === period
                                        ? "bg-blue-600 text-white"
                                        : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                                    }`
                                }
                            >
                                {period}
                            </button>
                        ))}
                    </div>
                </div>
                {/* Status Filter */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        주문 상태
                    </label>
                    <div className="flex flex-wrap gap-2">
                        {statuses.map((status) => (
                            <button
                                key={status}
                                onClick={() => setSelectedStatus(status)}
                                className={
                                    `px-4 py-2 rounded-lg text-sm font-medium cursor-pointer !rounded-button whitespace-nowrap 
                                    ${selectedStatus === status
                                        ? "bg-blue-600 text-white"
                                        : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                                    }`
                                }
                            >
                                {status}
                            </button>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OrderSearch;