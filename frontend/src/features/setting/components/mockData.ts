import type { SettingLanguage } from "../types/SettingLanguage";
import type { SettingTab } from "../types/SettingTab";
import type { SettingTheme } from "../types/SettingTheme";

export const tabs: SettingTab[] = [
    { id: "account", name: "계정 설정", icon: "fas fa-user-cog" },
    { id: "notification", name: "알림 설정", icon: "fas fa-bell" },
    { id: "language", name: "언어 설정", icon: "fas fa-globe" },
    { id: "theme", name: "테마 설정", icon: "fas fa-palette" },
    // { id: "privacy", name: "개인정보 설정", icon: "fas fa-shield-alt" },
    { id: "security", name: "보안 설정", icon: "fas fa-lock" },
    { id: "payment", name: "결제 설정", icon: "fas fa-credit-card" },
    // { id: "data", name: "데이터 관리", icon: "fas fa-database" },
];

export const languages: SettingLanguage[] = [
    { code: "ko", name: "한국어", flag: "🇰🇷" },
    { code: "en", name: "English", flag: "🇺🇸" },
    // { code: "ja", name: "日本語", flag: "🇯🇵" },
    // { code: "zh", name: "中文", flag: "🇨🇳" },
];

export const themes: SettingTheme[] = [
    { code: "light", name: "라이트 모드", icon: "fas fa-sun", desc: "밝은 테마를 사용합니다" },
    { code: "dark", name: "다크 모드", icon: "fas fa-moon", desc: "어두운 테마를 사용합니다" },
    // { code: "system", name: "시스템 설정", icon: "fas fa-laptop", desc: "시스템 설정을 따릅니다" },
];