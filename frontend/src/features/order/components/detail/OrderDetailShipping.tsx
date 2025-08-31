import type { OrderDetailShippingProps } from "../../types/OrderShipping";

const OrderDetailShipping = ({ shipping }: OrderDetailShippingProps) => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                배송 정보
            </h3>
            <div className="space-y-3">
                <div className="flex justify-between">
                    <span className="text-gray-600">
                        수령인
                    </span>
                    <span className="font-medium">
                        {shipping.recipient}
                    </span>
                </div>
                <div className="flex justify-between">
                    <span className="text-gray-600">
                        연락처
                    </span>
                    <span className="font-medium">
                        {shipping.phone}
                    </span>
                </div>
                <div>
                    <span className="text-gray-600">
                        배송지 주소
                    </span>
                    <div className="mt-1">
                        <p className="font-medium">
                            ({shipping.zipCode}) {shipping.address}
                        </p>
                        <p className="font-medium">
                            {shipping.addressDetail}
                        </p>
                    </div>
                </div>
                <div className="flex justify-between">
                    <span className="text-gray-600">
                        배송 요청사항
                    </span>
                    <span className="font-medium">
                        {shipping.request}
                    </span>
                </div>
                <div className="flex justify-between items-center">
                    <span className="text-gray-600">
                        운송장 번호
                    </span>
                    <div className="flex items-center space-x-2">
                        <span className="font-medium">
                            {shipping.trackingNumber}
                        </span>
                        <button
                            onClick={() => { }}
                            className="px-3 py-1 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 cursor-pointer !rounded-button whitespace-nowrap">
                            배송 추적
                        </button>
                    </div>
                </div>
                <div className="flex justify-between">
                    <span className="text-gray-600">
                        택배사
                    </span>
                    <span className="font-medium">
                        {shipping.courier}
                    </span>
                </div>
            </div>
        </div>
    );
};

export default OrderDetailShipping;