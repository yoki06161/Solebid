export interface SettingPrivacyProps {
    dataCollection: boolean;
    setDataCollection: (value: boolean) => void;
    personalizedAds: boolean;
    setPersonalizedAds: (value: boolean) => void;
}