import type { NotificationNavProps } from "../types/NotificationNavProps";

const NotificationNav = ({ tabs, activeTab, onTabChange }: NotificationNavProps) => (
    <div className="bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex space-x-8 overflow-x-auto">
                {tabs.map((tab) => (
                    <button
                        key={tab}
                        onClick={() => onTabChange(tab)}
                        className={
                            `py-4 px-1 border-b-2 font-medium text-sm whitespace-nowrap cursor-pointer 
                            ${activeTab === tab
                                ? "border-blue-500 text-blue-600"
                                : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                            }`}
                    >
                        {tab}
                    </button>
                ))}
            </div>
        </div>
    </div>
);

export default NotificationNav;