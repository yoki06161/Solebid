import type { OrderDetailPaymentProps } from "../../types/OrderPayment";

const OrderDetailPayment = ({ payment }: OrderDetailPaymentProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                결제 정보
            </h3>
            <div className="space-y-3">
                <div className="flex justify-between">
                    <span className="text-gray-600">
                        결제 방법
                    </span>
                    <span className="font-medium">
                        {payment.method}
                    </span>
                </div>
                <div className="flex justify-between">
                    <span className="text-gray-600">
                        카드 정보
                    </span>
                    <span className="font-medium">
                        {payment.cardInfo}
                    </span>
                </div>
                <div className="flex justify-between">
                    <span className="text-gray-600">
                        결제 상태
                    </span>
                    <span className="font-medium text-green-600">
                        {payment.status}
                    </span>
                </div>
                <div className="border-t border-gray-300 pt-3 mt-3">
                    <div className="flex justify-between mb-2">
                        <span className="text-gray-600">
                            상품 금액
                        </span>
                        <span>
                            {payment.itemAmount.toLocaleString()}원
                        </span>
                    </div>
                    <div className="flex justify-between mb-2">
                        <span className="text-gray-600">
                            배송비
                        </span>
                        <span>
                            {payment.shippingFee.toLocaleString()}원
                        </span>
                    </div>
                    <div className="flex justify-between mb-2">
                        <span className="text-gray-600">
                            할인 금액
                        </span>
                        <span className="text-red-600">
                            -{payment.discount.toLocaleString()}원
                        </span>
                    </div>
                    <div className="flex justify-between font-bold text-lg border-t border-gray-300 pt-2">
                        <span>
                            최종 결제 금액
                        </span>
                        <span>
                            {payment.finalAmount.toLocaleString()}원
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OrderDetailPayment;