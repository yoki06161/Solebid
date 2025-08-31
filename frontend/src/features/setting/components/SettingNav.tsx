import type { SettingNavProps } from "../types/SettingNavProps";
import type { SettingTab } from "../types/SettingTab";
import { tabs } from "./mockData";

const SettingsNav = ({ activeTab, onTabClick }: SettingNavProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                설정 메뉴
            </h3>
            <nav className="space-y-1">
                {tabs.map((tab: SettingTab) => (
                    <button
                        key={tab.id}
                        onClick={() => onTabClick(tab.id)}
                        className={
                            `w-full flex items-center px-3 py-2 text-left rounded-lg 
                            ${activeTab === tab.id
                                ? 'bg-blue-50 text-blue-700 border border-blue-600'
                                : 'text-gray-700 hover:bg-gray-100'
                            }`
                        }
                    >
                        <i className={`${tab.icon} w-5 text-center mr-3`} />
                        {tab.name}
                    </button>
                ))}
            </nav>
        </div>
    );
};

export default SettingsNav;