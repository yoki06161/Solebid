import { useState } from "react";
import CartEmptyList from "../components/CartEmptyList";
import CartList from "../components/CartList";
import CartSummary from "../components/CartSummary";
import { useCart } from "../hooks/useCart";

const CartPage = () => {
    const { cartItems, loading, error, removeItem } = useCart();
    const [isEditing, setIsEditing] = useState(false);

    if (loading) {
        return (
            <div className="fixed top-0 left-0 w-full h-full flex justify-center items-center">
                <i className="fas fa-spinner fa-spin fa-3x"></i>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <p className="text-red-600 mb-4">{error}</p>
                    <button
                        onClick={() => window.location.reload()}
                        className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                    >
                        다시 시도
                    </button>
                </div>
            </div>
        );
    }

    const totalAmount = cartItems.reduce(
        (sum, item) => sum + item.productPrice,
        0,
    );
    const shippingFee = totalAmount >= 50000 ? 0 : 3000;
    const finalAmount = totalAmount + shippingFee;

    const formatPrice = (price: number) => price.toLocaleString("ko-KR") + "원";

    if (cartItems.length === 0) {
        return <CartEmptyList />;
    }
    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-3xl mx-auto">
                <main className="pb-24">
                    <CartList
                        items={cartItems}
                        isEditing={isEditing}
                        onToggleEdit={() => setIsEditing(!isEditing)}
                        onRemoveItem={removeItem}
                    />
                    <div className="mx-4 my-6">
                        <CartSummary
                            totalAmount={totalAmount}
                            shippingFee={shippingFee}
                            finalAmount={finalAmount}
                            formatPrice={formatPrice}
                        />
                    </div>
                </main>
                <div className="fixed bottom-0 left-0 right-0 max-w-3xl mx-auto">
                    <div className="p-4 bg-white">
                        <button
                            onClick={() => { }}
                            className="w-full bg-blue-600 text-white py-4 font-semibold text-lg cursor-pointer whitespace-nowrap hover:bg-blue-700 transition-colors rounded-lg"
                        >
                            결제하기 · {formatPrice(finalAmount)}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CartPage;
