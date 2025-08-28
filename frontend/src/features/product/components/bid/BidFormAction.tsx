import { useNavigate } from 'react-router-dom';
import type { BidFormActionProps } from '../../types/bid/BidFormActionProps';

const BidFormAction = ({ onSubmit }: BidFormActionProps) => {
    const navigate = useNavigate();
    return (
        <div className="fixed bottom-0 left-0 right-0 bg-white border-t">
            <div className="max-w-3xl mx-auto px-6 py-4 flex space-x-4">
                <button
                    onClick={() => navigate("/")}
                    className="flex-1 w-full px-6 py-3 border border-gray-300 text-gray-700 !rounded-button"
                >
                    취소
                </button>
                <button
                    onClick={onSubmit}
                    className="flex-1 w-full px-6 py-3 bg-blue-500 text-white !rounded-button"
                >
                    등록하기
                </button>
            </div>
        </div>
    );
};

export default BidFormAction;