import type { NotificationSearchProps } from "../types/NotificationSearchProps";

const NotificationSearch = ({ sortBy, onSortChange, searchQuery, onSearchChange }: NotificationSearchProps) => (
    <div className="bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
                <div className="flex items-center space-x-4">
                    <select
                        value={sortBy}
                        onChange={(e) => onSortChange(e.target.value)}
                        className="border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent cursor-pointer"
                    >
                        <option value="마감임박순">마감임박순</option>
                        <option value="신규순">신규순</option>
                    </select>
                </div>
                <div className="relative">
                    <input
                        type="text"
                        placeholder="상품명으로 검색..."
                        value={searchQuery}
                        onChange={(e) => onSearchChange(e.target.value)}
                        className="w-full sm:w-80 pl-10 pr-4 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                    <i className="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-sm" />
                </div>
            </div>
        </div>
    </div>
);

export default NotificationSearch;