import { formatDate, formatPrice } from "../../../utils/bid-utils";
import type { ProfileBidSellingProps } from "../types/ProfileBidSellingProps";

const ProfileTransactionItem = ({
    productName,
    soldDate,
    soldPrice,
    imageUrl
}: ProfileBidSellingProps) => {
    return (
        <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
            <div className="flex items-center">
                <img
                    src={imageUrl || '/placeholder-image.jpg'}
                    alt={productName}
                    className="w-12 h-12 rounded-lg object-cover mr-4" />
                <div>
                    <h4 className="font-medium text-gray-900">
                        {productName}
                    </h4>
                    <p className="text-gray-600 text-sm">
                        {formatDate(soldDate)}
                    </p>
                </div>
            </div>
            <div className="text-right">
                <div className="font-semibold text-gray-900">
                    {formatPrice(soldPrice)}
                </div>
            </div>
        </div>
    );
}

export default ProfileTransactionItem;
