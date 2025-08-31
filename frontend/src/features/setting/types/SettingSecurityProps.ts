export interface SettingSecurityProps {
    twoFactorAuth: boolean;
    setTwoFactorAuth: (value: boolean) => void;
}