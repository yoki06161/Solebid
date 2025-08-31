import type { SettingThemeProps } from "../types/SettingTheme";
import { themes } from "./mockData";

const SettingTheme = ({ selectedTheme, setSelectedTheme }: SettingThemeProps) => {
    return (
        <div className="space-y-6">
            <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    테마 설정
                </h3>
                <div className="space-y-2">
                    {themes.map((theme) => (
                        <button
                            key={theme.code}
                            onClick={() => setSelectedTheme(theme.code)}
                            className={
                                `w-full text-left px-4 py-3 rounded-lg border cursor-pointer
                                ${selectedTheme === theme.code
                                    ? "border-blue-500 bg-blue-50 text-blue-700"
                                    : "border-gray-200 hover:bg-gray-50"
                                }`
                            }
                        >
                            <div className="flex items-center">
                                <i className=
                                    {`${theme.icon} mr-3 text-lg`}
                                />
                                <div className="flex-1">
                                    <div className="font-medium">
                                        {theme.name}
                                    </div>
                                    <div className="text-sm text-gray-600">
                                        {theme.desc}
                                    </div>
                                </div>
                                {selectedTheme === theme.code && (
                                    <i className="fas fa-check text-blue-600" />
                                )}
                            </div>
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default SettingTheme;