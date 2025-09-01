const SettingAccount = () => {
    return (
        <div className="space-y-6">
            <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    기본 정보
                </h3>
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            이름
                        </label>
                        <input
                            type="text"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                            defaultValue="김민수"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            이메일
                        </label>
                        <input
                            type="email"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                            defaultValue="minsu.kim@email.com"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            전화번호
                        </label>
                        <input
                            type="tel"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                            defaultValue="010-1234-5678"
                        />
                    </div>
                </div>
            </div>
            <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    비밀번호 변경
                </h3>
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            현재 비밀번호
                        </label>
                        <input
                            type="password"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                            placeholder="현재 비밀번호를 입력하세요"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            새 비밀번호
                        </label>
                        <input
                            type="password"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                            placeholder="새 비밀번호를 입력하세요"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            새 비밀번호 확인
                        </label>
                        <input
                            type="password"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                            placeholder="새 비밀번호를 다시 입력하세요"
                        />
                    </div>
                </div>
            </div>
            <div className="flex justify-end space-x-3">
                <button
                    onClick={() => { }}
                    className="px-6 py-2 text-gray-600 hover:text-gray-900 rounded-lg cursor-pointer whitespace-nowrap shadow-sm">
                    취소
                </button>
                <button
                    onClick={() => { }}
                    className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 cursor-pointer whitespace-nowrap shadow-sm">
                    저장
                </button>
            </div>
        </div>
    );
};

export default SettingAccount;