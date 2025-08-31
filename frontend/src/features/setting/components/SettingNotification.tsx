import type { SettingNotificationProps } from "../types/SettingNotificationProps";

const SettingNotification = ({
    emailNotifications,
    setEmailNotifications,
    pushNotifications,
    setPushNotifications,
}: SettingNotificationProps) => {

    const Toggle = (
        { checked, onChange }: {
            checked: boolean;
            onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
        }
    ) => (
        <label className="relative inline-flex items-center cursor-pointer">
            <input
                type="checkbox"
                checked={checked}
                onChange={onChange}
                className="sr-only peer"
            />
            <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
        </label>
    );

    return (
        <div className="space-y-6">
            <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    알림 설정
                </h3>
                <div className="space-y-4">
                    <div className="flex items-center justify-between p-4 border border-gray-300 rounded-lg">
                        <div>
                            <h4 className="font-medium text-gray-900">
                                이메일 알림
                            </h4>
                            <p className="text-sm text-gray-600">
                                주문, 배송, 프로모션 관련 이메일을 받습니다
                            </p>
                        </div>
                        <Toggle
                            checked={emailNotifications}
                            onChange={(e) => setEmailNotifications(e.target.checked)}
                        />
                    </div>
                    <div className="flex items-center justify-between p-4 border border-gray-300 rounded-lg">
                        <div>
                            <h4 className="font-medium text-gray-900">
                                푸시 알림
                            </h4>
                            <p className="text-sm text-gray-600">
                                브라우저 푸시 알림을 받습니다
                            </p>
                        </div>
                        <Toggle
                            checked={pushNotifications}
                            onChange={(e) => setPushNotifications(e.target.checked)}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SettingNotification;