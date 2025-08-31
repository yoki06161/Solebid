
const SettingPayment = () => {
    return (
        <div className="space-y-6">
            <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    결제 수단 관리
                </h3>
                <div className="space-y-4">
                    <div className="p-4 border border-gray-300 rounded-lg">
                        <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center">
                                <i className="fab fa-cc-visa text-2xl text-blue-600 mr-3" />
                                <div>
                                    <h4 className="font-medium text-gray-900">
                                        VISA 카드
                                    </h4>
                                    <p className="text-sm text-gray-600">
                                        **** **** **** 1234
                                    </p>
                                </div>
                            </div>
                            <div className="flex space-x-2">
                                <button
                                    onClick={() => { }}
                                    className="px-3 py-1 text-sm text-blue-600 hover:text-blue-800 cursor-pointer">
                                    수정
                                </button>
                                <button
                                    onClick={() => { }}
                                    className="px-3 py-1 text-sm text-red-600 hover:text-red-800 cursor-pointer">
                                    삭제
                                </button>
                            </div>
                        </div>
                        <div className="text-sm text-gray-600">
                            만료일: 12/26 | 기본 결제 수단
                        </div>
                    </div>
                    <div className="p-4 border border-gray-300 rounded-lg">
                        <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center">
                                <i className="fab fa-cc-mastercard text-2xl text-red-600 mr-3" />
                                <div>
                                    <h4 className="font-medium text-gray-900">
                                        MasterCard
                                    </h4>
                                    <p className="text-sm text-gray-600">
                                        **** **** **** 5678
                                    </p>
                                </div>
                            </div>
                            <div className="flex space-x-2">
                                <button
                                    onClick={() => { }}
                                    className="px-3 py-1 text-sm text-blue-600 hover:text-blue-800 cursor-pointer">
                                    수정
                                </button>
                                <button
                                    onClick={() => { }}
                                    className="px-3 py-1 text-sm text-red-600 hover:text-red-800 cursor-pointer">
                                    삭제
                                </button>
                            </div>
                        </div>
                        <div className="text-sm text-gray-600">
                            만료일: 08/27
                        </div>
                    </div>
                    <button
                        onClick={() => { }}
                        className="w-full p-4 border-2 border-dashed border-gray-300 rounded-lg text-gray-600 hover:border-gray-400 hover:text-gray-700 cursor-pointer">
                        <i className="fas fa-plus mr-2" />
                        새 결제 수단 추가
                    </button>
                </div>
            </div>
            <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    자동 결제 설정
                </h3>
                <div className="p-4 border border-gray-300 rounded-lg">
                    <div className="flex items-center justify-between">
                        <div>
                            <h4 className="font-medium text-gray-900">
                                정기 구독 자동 결제
                            </h4>
                            <p className="text-sm text-gray-600">
                                매월 자동으로 구독료가 결제됩니다
                            </p>
                        </div>
                        <label className="relative inline-flex items-center cursor-pointer">
                            <input
                                type="checkbox"
                                className="sr-only peer"
                                defaultChecked
                            />
                            <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                        </label>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SettingPayment;