import React from "react";

const SecurityInfo: React.FC = () => (
    <div className="bg-white rounded-lg shadow-sm border border-gray-300 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">보안 정보</h3>
        <div className="space-y-3">
            <div className="flex items-center space-x-3">
                <i className="fas fa-shield-alt text-green-500" />
                <span className="text-sm text-gray-600">SSL 보안 연결</span>
            </div>
            <div className="flex items-center space-x-3">
                <i className="fas fa-lock text-green-500" />
                <span className="text-sm text-gray-600">개인정보 암호화</span>
            </div>
            <div className="flex items-center space-x-3">
                <i className="fas fa-certificate text-green-500" />
                <span className="text-sm text-gray-600">PG사 인증</span>
            </div>
        </div>
    </div>
);

export default SecurityInfo;
