export interface SettingLanguage {
    code: string;
    name: string;
    flag: string;
}

export interface SettingLanguageProps {
    selectedLanguage: string;
    setSelectedLanguage: (value: string) => void;
}