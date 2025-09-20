import { getFormatPrice } from "../../../utils/get-format-price";
import type { CartItemProps } from "../types/CartItemProps";

const CartItem = ({ item, isEditing, onRemoveItem }: CartItemProps) => {
    return (
        <div className="bg-white rounded-lg p-4 shadow-sm border border-gray-100">
            <div className="flex items-start space-x-4">
                <div className="w-20 h-20 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
                    <img
                        src={item.imageUrl || '/placeholder-image.jpg'}
                        alt={item.productName}
                        className="w-full h-full object-cover object-top"
                    />
                </div>
                <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-start mb-2">
                        <h3 className="text-base font-medium text-gray-900 truncate pr-2">
                            {item.productName}
                        </h3>
                        {isEditing && (
                            <button
                                onClick={() => onRemoveItem(item.cartId)}
                                className="text-gray-400 hover:text-red-500">
                                <i className="fas fa-times text-lg"></i>
                            </button>
                        )}
                    </div>
                    <div className="text-sm text-gray-500 mb-1">
                        {item.productBrand} · {item.productCategory} · {item.productSize}mm
                    </div>
                    <p className="text-lg font-semibold text-gray-900 mb-3">
                        {getFormatPrice(item.productPrice)}
                    </p>
                </div>
            </div>
        </div>
    );
}

export default CartItem;