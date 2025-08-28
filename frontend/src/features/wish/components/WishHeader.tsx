import BackPress from "../../../../components/BackPress";
import type { WishHeaderProps } from "../../types/wish/WishHeaderProps";

const WishHeader: React.FC<WishHeaderProps> = ({ itemCount }) => (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
                <BackPress className="mr-4 text-gray-600 hover:text-gray-900 cursor-pointer">
                    <i className="fas fa-arrow-left text-lg" />
                </BackPress>
                <h1 className="text-2xl font-bold text-gray-900">찜한 상품</h1>
            </div>
            <div className="text-gray-600 text-sm">총 {itemCount}개 상품</div>
        </div>
    </div>
);

export default WishHeader;