export interface SettingNotificationProps {
    emailNotifications: boolean;
    setEmailNotifications: (value: boolean) => void;
    pushNotifications: boolean;
    setPushNotifications: (value: boolean) => void;
}