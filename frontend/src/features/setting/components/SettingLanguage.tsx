import type { SettingLanguageProps } from "../types/SettingLanguage";
import { languages } from "./mockData";

const SettingLanguage = ({ selectedLanguage, setSelectedLanguage }: SettingLanguageProps) => {
    return (
        <div className="space-y-6">
            <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    언어 설정
                </h3>
                <div className="space-y-2">
                    {languages.map((lang) => (
                        <button
                            key={lang.code}
                            onClick={() => setSelectedLanguage(lang.code)}
                            className={
                                `w-full text-left px-4 py-3 rounded-lg border flex items-center cursor-pointer 
                                ${selectedLanguage === lang.code
                                    ? "border-blue-500 bg-blue-50 text-blue-700"
                                    : "border-gray-200 hover:bg-gray-50"
                                }`
                            }
                        >
                            <span className="mr-3 text-xl">
                                {lang.flag}
                            </span>
                            <span className="font-medium">
                                {lang.name}
                            </span>
                            {selectedLanguage === lang.code && (
                                <i className="fas fa-check ml-auto text-blue-600" />
                            )}
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default SettingLanguage;