import type { RankingTabProps } from "../../types/ranking/RankingTabProps";
import { tabs } from "./mockData";

const RankingTab: React.FC<RankingTabProps> = ({ activeTab, onTabClick }) => {
    return (
        <div className="flex space-x-4 border-b border-gray-200 mb-8">
            {tabs.map((tab) => (
                <button
                    key={tab.id}
                    onClick={() => onTabClick(tab.id)}
                    className={
                        `px-6 py-4 font-medium !rounded-button whitespace-nowrap
                         ${activeTab === tab.id
                            ? "text-blue-600 border-b-2 border-blue-600"
                            : "text-gray-600"
                        }`
                    }
                >
                    {tab.label}
                </button>
            ))}
        </div>
    );
};

export default RankingTab;