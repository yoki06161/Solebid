//리다이렉트 테스트를 위한 임시 페이지

import React from "react";
import { useLocation } from "react-router-dom";

function useQuery() {
    const { search } = useLocation();
    return React.useMemo(() => new URLSearchParams(search), [search]);
}

const ChargeResultPage: React.FC = () => {
    const q = useQuery();
    const success = q.get("success");
    const impUid = q.get("imp_uid");
    const merchantUid = q.get("merchant_uid");
    const errorMsg = q.get("error_msg");

    return (
        <div className="max-w-xl mx-auto px-6 py-12">
            <h1 className="text-2xl font-bold mb-4">결제 결과</h1>
            <div className="bg-white border rounded-lg p-6 space-y-2">
                <div><span className="font-medium">성공 여부:</span> {success ?? "-"}</div>
                <div><span className="font-medium">imp_uid:</span> {impUid ?? "-"}</div>
                <div><span className="font-medium">merchant_uid:</span> {merchantUid ?? "-"}</div>
                {errorMsg && (
                    <div className="text-red-600"><span className="font-medium">오류:</span> {errorMsg}</div>
                )}
            </div>
            <p className="text-gray-500 text-sm mt-4">
                * 실제 승인/검증은 서버에서 수행합니다. 이 페이지는 리다이렉트 파라미터만 표시합니다.
            </p>
        </div>
    );
};

export default ChargeResultPage;
