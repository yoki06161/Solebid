import type { SettingSidebarProps } from "../types/SettingSidebarProps";
import SettingNav from "./SettingNav";
import SettingProfile from "./SettingProfile";

const SettingSidebar = ({ activeTab, onTabClick }: SettingSidebarProps) => {
    return (
        <div className="col-span-3">
            <SettingProfile />
            <SettingNav
                activeTab={activeTab}
                onTabClick={onTabClick}
            />
        </div>
    );
};

export default SettingSidebar;