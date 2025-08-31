import React, { useState } from "react";
import { SettingAccount, SettingLanguage, SettingNotification, SettingPayment, SettingSecurity, SettingSidebar, SettingTheme } from "../components";

const SettingPage = () => {
    const [activeTab, setActiveTab] = useState("account");
    const [emailNotifications, setEmailNotifications] = useState(true);
    const [pushNotifications, setPushNotifications] = useState(true);
    const [selectedLanguage, setSelectedLanguage] = useState("ko");
    const [selectedTheme, setSelectedTheme] = useState("light");
    const [twoFactorAuth, setTwoFactorAuth] = useState(false);

    const renderTabContent = () => {
        const tabComponents: { [key: string]: React.ReactNode } = {
            account: <SettingAccount />,
            notification: (
                <SettingNotification
                    emailNotifications={emailNotifications}
                    setEmailNotifications={setEmailNotifications}
                    pushNotifications={pushNotifications}
                    setPushNotifications={setPushNotifications}
                />
            ),
            language: (
                <SettingLanguage
                    selectedLanguage={selectedLanguage}
                    setSelectedLanguage={setSelectedLanguage}
                />
            ),
            theme: (
                <SettingTheme
                    selectedTheme={selectedTheme}
                    setSelectedTheme={setSelectedTheme}
                />
            ),
            security: (
                <SettingSecurity
                    twoFactorAuth={twoFactorAuth}
                    setTwoFactorAuth={setTwoFactorAuth}
                />
            ),
            payment: <SettingPayment />,
        };

        return tabComponents[activeTab] || null;
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="grid grid-cols-12 gap-8">
                    <SettingSidebar
                        activeTab={activeTab}
                        onTabClick={setActiveTab}
                    />
                    <div className="col-span-9">
                        <div className="bg-white rounded-lg shadow-sm p-8">
                            {renderTabContent()}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default SettingPage;