import type { SettingSecurityProps } from '../types/SettingSecurityProps';

const SettingSecurity = ({ twoFactorAuth, setTwoFactorAuth }: SettingSecurityProps) => {
    return (
        <div className="space-y-6">
            <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    보안 설정
                </h3>
                <div className="space-y-4">
                    <div className="flex items-center justify-between p-4 border border-gray-300 rounded-lg">
                        <div>
                            <h4 className="font-medium text-gray-900">
                                2단계 인증
                            </h4>
                            <p className="text-sm text-gray-600">
                                계정 보안을 위해 2단계 인증을 활성화합니다
                            </p>
                        </div>
                        <label className="relative inline-flex items-center cursor-pointer">
                            <input
                                type="checkbox"
                                checked={twoFactorAuth}
                                onChange={(e) => setTwoFactorAuth(e.target.checked)}
                                className="sr-only peer"
                            />
                            <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                        </label>
                    </div>
                    <div className="p-4 border border-gray-300 rounded-lg">
                        <h4 className="font-medium text-gray-900 mb-2">
                            로그인 기록
                        </h4>
                        <div className="space-y-2">
                            <div className="flex justify-between items-center text-sm">
                                <span className="text-gray-600">
                                    Chrome - Windows
                                </span>
                                <span className="text-gray-500">
                                    2024.01.15 14:30
                                </span>
                            </div>
                            <div className="flex justify-between items-center text-sm">
                                <span className="text-gray-600">
                                    Safari - iPhone
                                </span>
                                <span className="text-gray-500">
                                    2024.01.14 09:15
                                </span>
                            </div>
                            <div className="flex justify-between items-center text-sm">
                                <span className="text-gray-600">
                                    Chrome - Android
                                </span>
                                <span className="text-gray-500">
                                    2024.01.13 18:45
                                </span>
                            </div>
                        </div>
                        <button
                            onClick={() => { }}
                            className="mt-3 text-blue-600 text-sm hover:text-blue-800 cursor-pointer">
                            전체 로그인 기록 보기
                        </button>
                    </div>
                    <div className="p-4 border border-gray-300 rounded-lg">
                        <h4 className="font-medium text-gray-900 mb-2">
                            활성 세션
                        </h4>
                        <p className="text-sm text-gray-600 mb-3">
                            현재 로그인된 기기들입니다
                        </p>
                        <button
                            onClick={() => { }}
                            className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 cursor-pointer !rounded-button whitespace-nowrap">
                            모든 기기에서 로그아웃
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SettingSecurity;