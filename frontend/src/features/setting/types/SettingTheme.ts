export interface SettingTheme {
    code: string;
    name: string;
    icon: string;
    desc: string;
}

export interface SettingThemeProps {
    selectedTheme: string;
    setSelectedTheme: (value: string) => void;
}