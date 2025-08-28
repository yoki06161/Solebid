import { useNavigate } from 'react-router-dom';
import type { BidHeaderProps } from '../../types/bid/BidHeaderProps';

const BidHeader = ({ title }: BidHeaderProps) => {
    const navigate = useNavigate();
    return (
        <header className="h-16 px-6 flex items-center border-b bg-white sticky top-0 z-10">
            <button
                onClick={() => navigate(-1)}
                className="flex items-center text-gray-600 hover:text-gray-900"
            >
                <i className="fas fa-arrow-left mr-2" />
                <span>
                    뒤로가기
                </span>
            </button>
            <h1 className="text-xl font-bold text-center flex-1">
                {title}
            </h1>
        </header>
    );
};

export default BidHeader;