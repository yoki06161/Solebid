import type { TransactionSearchProps } from "../types/TransactionSearchProps";

const TransactionSearch = ({
    searchQuery,
    setSearchQuery,
    showDateDropdown,
    setShowDateDropdown,
    setSelectedDateFilter,
    showStatusDropdown,
    setShowStatusDropdown,
    setSelectedStatusFilter,
    selectedDateFilter,
    selectedStatusFilter,
}: TransactionSearchProps) => {

    const dateFilterOptions: { [key: string]: string } = {
        "all": "전체 기간",
        "week": "최근 1주일",
        "month": "최근 1개월",
        "3months": "최근 3개월",
    }

    const statusFilterOptions: { [key: string]: string } = {
        "all": "전체 상태",
        "completed": "판매 완료",
        "shipping": "배송중",
        "cancelled": "취소/반품",
    }

    const handleDateFilterClick = () => {
        setShowDateDropdown(!showDateDropdown);
        setShowStatusDropdown(false);
    };

    const handleStatusFilterClick = () => {
        setShowStatusDropdown(!showStatusDropdown);
        setShowDateDropdown(false);
    };

    const handleDateSelect = (filter: string) => {
        setSelectedDateFilter(filter);
        setShowDateDropdown(false);
    };

    const handleStatusSelect = (filter: string) => {
        setSelectedStatusFilter(filter);
        setShowStatusDropdown(false);
    };

    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between space-y-4 lg:space-y-0 lg:space-x-4">
                <div className="relative flex-1 max-w-md">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <i className="fas fa-search text-gray-400 text-sm" />
                    </div>
                    <input
                        type="text"
                        placeholder="상품명으로 검색"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                    />
                </div>
                <div className="flex space-x-4">
                    <div className="relative">
                        <button
                            onClick={handleDateFilterClick}
                            className="flex items-center px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 cursor-pointer !rounded-button whitespace-nowrap"
                        >
                            <i className="fas fa-calendar-alt mr-2 text-gray-500 text-sm" />
                            <span className="text-sm">
                                {dateFilterOptions[selectedDateFilter]}
                            </span>
                            <i className="fas fa-chevron-down ml-2 text-gray-400 text-xs" />
                        </button>
                        {showDateDropdown && (
                            <div className="absolute top-full left-0 mt-1 w-48 bg-white rounded-lg shadow-lg border border-gray-200 z-10">
                                <div className="py-2">
                                    <button
                                        onClick={() => handleDateSelect("all")}
                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                    >
                                        전체 기간
                                    </button>
                                    <button
                                        onClick={() => handleDateSelect("week")}
                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                    >
                                        최근 1주일
                                    </button>
                                    <button
                                        onClick={() => handleDateSelect("month")}
                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                    >
                                        최근 1개월
                                    </button>
                                    <button
                                        onClick={() => handleDateSelect("3months")}
                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                    >
                                        최근 3개월
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                    <div className="relative">
                        <button
                            onClick={handleStatusFilterClick}
                            className="flex items-center px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 cursor-pointer !rounded-button whitespace-nowrap"
                        >
                            <i className="fas fa-filter mr-2 text-gray-500 text-sm" />
                            <span className="text-sm">
                                {statusFilterOptions[selectedStatusFilter]}
                            </span>
                            <i className="fas fa-chevron-down ml-2 text-gray-400 text-xs" />
                        </button>
                        {showStatusDropdown && (
                            <div className="absolute top-full left-0 mt-1 w-48 bg-white rounded-lg shadow-lg border border-gray-200 z-10">
                                <div className="py-2">
                                    <button
                                        onClick={() => handleStatusSelect("all")}
                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                    >
                                        전체 상태
                                    </button>
                                    <button
                                        onClick={() => handleStatusSelect("completed")}
                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                    >
                                        판매 완료
                                    </button>
                                    <button
                                        onClick={() => handleStatusSelect("shipping")}
                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                    >
                                        배송중
                                    </button>
                                    <button
                                        onClick={() => handleStatusSelect("cancelled")}
                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                    >
                                        취소/반품
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TransactionSearch;
