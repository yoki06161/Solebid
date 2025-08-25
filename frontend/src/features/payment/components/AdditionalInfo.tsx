import React from "react";

const AdditionalInfo: React.FC = () => (
    <div className="mt-12 bg-white rounded-lg shadow-sm border border-gray-300 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">포인트 충전 안내사항</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <h4 className="font-medium text-gray-900 mb-2">충전 관련</h4>
                <ul className="text-sm text-gray-600 space-y-1">
                    <li>• 최소 충전 금액: 1,000원</li>
                    <li>• 최대 충전 금액: 1,000,000원</li>
                    <li>• 충전 즉시 포인트 적립</li>
                    <li>• 1원 = 1포인트 동일 전환</li>
                </ul>
            </div>
            <div>
                <h4 className="font-medium text-gray-900 mb-2">환불 정책</h4>
                <ul className="text-sm text-gray-600 space-y-1">
                    <li>• 미사용 포인트 환불 가능</li>
                    <li>• 환불 수수료 없음</li>
                    <li>• 영업일 기준 3-5일 소요</li>
                    <li>• 고객센터를 통한 신청</li>
                </ul>
            </div>
        </div>

        <div className="mt-6 pt-6 border-t border-gray-300">
            <div className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                    <i className="fas fa-headset text-blue-500" />
                    <div>
                        <div className="font-medium text-gray-900">고객센터</div>
                        <div className="text-sm text-gray-600">1588-1234 (평일 09:00-18:00)</div>
                    </div>
                </div>
                <div className="flex space-x-4">
                    <button type="button" className="!rounded-button whitespace-nowrap cursor-pointer text-sm text-blue-600 hover:text-blue-700">
                        개인정보처리방침
                    </button>
                    <button type="button" className="!rounded-button whitespace-nowrap cursor-pointer text-sm text-blue-600 hover:text-blue-700">
                        이용약관
                    </button>
                </div>
            </div>
        </div>
    </div>
);

export default AdditionalInfo;
