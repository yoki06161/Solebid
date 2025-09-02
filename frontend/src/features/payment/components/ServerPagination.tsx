import React from 'react';

interface Props {
    page0: number;       // 0-based
    totalPages: number;
    onPrev: () => void;
    onNext: () => void;
}

const ServerPagination: React.FC<Props> = ({ page0, totalPages, onPrev, onNext }) => {
    const currentPage = page0 + 1; // 1-based 표기
    return (
        <div className="flex items-center justify-end mt-6">
            <div className="flex items-center space-x-2">
                <button
                    onClick={onPrev}
                    className="px-3 py-2 border rounded-lg text-sm text-gray-600 hover:bg-gray-50 !rounded-button whitespace-nowrap"
                    disabled={page0 <= 0}
                >
                    이전
                </button>
                <span className="px-4 py-2 text-sm text-gray-700">
          {currentPage} / {Math.max(1, totalPages)}
        </span>
                <button
                    onClick={onNext}
                    className="px-3 py-2 border rounded-lg text-sm text-gray-600 hover:bg-gray-50 !rounded-button whitespace-nowrap"
                    disabled={currentPage >= totalPages}
                >
                    다음
                </button>
            </div>
        </div>
    );
};

export default ServerPagination;
