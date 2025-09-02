import React from 'react';

interface PaginationProps {
    currentPage: number;
    totalItems: number;
    itemsPerPage: number;
    onPrev: () => void;
    onNext: () => void;
}

const Pagination: React.FC<PaginationProps> = ({
                                                   currentPage,
                                                   totalItems,
                                                   itemsPerPage,
                                                   onPrev,
                                                   onNext,
                                               }) => {
    const totalPages = Math.max(1, Math.ceil(totalItems / itemsPerPage));

    return (
        <div className="flex items-center justify-end mt-6">
            <div className="flex items-center space-x-2">
                <button
                    onClick={onPrev}
                    className="px-3 py-2 border rounded-lg text-sm text-gray-600 hover:bg-gray-50 !rounded-button whitespace-nowrap"
                    disabled={currentPage === 1}
                >
                    이전
                </button>
                <span className="px-4 py-2 text-sm text-gray-700">
          {currentPage} / {totalPages}
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

export default Pagination;
