import { Link } from "react-router-dom";

const WishEmptyList = () => (
    <div className="bg-white rounded-lg shadow-sm p-12 text-center">
        <i className="fas fa-heart text-gray-300 text-6xl mb-4" />
        <h3 className="text-xl font-medium text-gray-900 mb-2">
            찜한 상품이 없습니다
        </h3>
        <p className="text-gray-600 mb-6">
            마음에 드는 상품을 찜해보세요!
        </p>
        <Link
            to="https://readdy.ai/home/..."
            className="inline-flex items-center px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 cursor-pointer !rounded-button whitespace-nowrap"
        >
            <i className="fas fa-shopping-bag mr-2" />
            쇼핑하러 가기
        </Link>
    </div>
);

export default WishEmptyList;