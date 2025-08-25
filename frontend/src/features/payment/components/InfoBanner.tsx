import React from "react";

const InfoBanner: React.FC = () => (
    <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
        <div className="flex items-start space-x-3">
            <i className="fas fa-info-circle text-blue-500 mt-1" />
            <div>
                <h3 className="font-semibold text-blue-900 mb-2">포인트 충전 안내</h3>
                <p className="text-sm text-blue-700 leading-relaxed">
                    충전된 포인트로 경매에 참여하고 상품을 구매하실 수 있습니다.
                    1원 = 1포인트로 전환되며, 충전 즉시 사용 가능합니다.
                </p>
            </div>
        </div>
    </div>
);

export default InfoBanner;
