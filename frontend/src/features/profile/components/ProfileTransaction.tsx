import { Link } from "react-router-dom";
import { useImageUrls } from "../../../hooks/useProductImageUrls";
import { useProfileBidSelling } from "../hooks/useProfileBidSelling";
import ProfileTransactionItem from "./ProfileTransactionItem";

const ProfileTransactionEmpty = () => (
    <div className="text-center py-8 text-gray-500">
        <p>판매 내역이 없습니다.</p>
    </div>
);

const ProfileTransactionLoading = () => (
    <div className="space-y-4">
        {[1, 2, 3].map(i => (
            <div key={i} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg animate-pulse">
                <div className="flex items-center">
                    <div className="w-12 h-12 bg-gray-300 rounded-lg mr-4"></div>
                    <div>
                        <div className="h-4 bg-gray-300 rounded w-24 mb-2"></div>
                        <div className="h-3 bg-gray-300 rounded w-16"></div>
                    </div>
                </div>
                <div className="h-4 bg-gray-300 rounded w-20"></div>
            </div>
        ))}
    </div>
);

const ProfileTransactionError = ({ error }: { error: string }) => (
    <div className="text-center py-8 text-red-500">
        <p>{error}</p>
    </div>
);

const ProfileTransaction = () => {
    const { soldProducts, loading, error } = useProfileBidSelling();
    const { itemsWithImages: productsWithImages, isLoadingImages } = useImageUrls(
        soldProducts,
        (item) => item.productImageUrl
    );

    const MAX_DISPLAY_COUNT = 3;

    if (loading || isLoadingImages) {
        return (
            <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-lg font-semibold text-gray-900">
                        최근 판매 내역
                    </h3>
                    <Link
                        to="/transaction"
                        className="text-blue-600 text-sm hover:text-blue-800 cursor-pointer">
                        전체 보기
                    </Link>
                </div>
                <ProfileTransactionLoading />
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-lg font-semibold text-gray-900">
                        최근 판매 내역
                    </h3>
                    <Link
                        to="/transaction"
                        className="text-blue-600 text-sm hover:text-blue-800 cursor-pointer">
                        전체 보기
                    </Link>
                </div>
                <ProfileTransactionError error={error} />
            </div>
        );
    }

    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold text-gray-900">
                    최근 판매 내역
                </h3>
                <Link
                    to="/transaction"
                    className="text-blue-600 text-sm hover:text-blue-800 cursor-pointer">
                    전체 보기
                </Link>
            </div>
            <div className="space-y-4">
                {productsWithImages.length === 0 ? (
                    <ProfileTransactionEmpty />
                ) : (
                    productsWithImages.slice(0, MAX_DISPLAY_COUNT).map(product => (
                        <ProfileTransactionItem
                            key={product.productId}
                            {...product}
                        />
                    ))
                )}
            </div>
        </div>
    );
};

export default ProfileTransaction;