import { Fragment, useEffect, useState } from "react";
import type { BrandModalProps } from "../../types/brand/BrandModalProps";

const BrandModal: React.FC<BrandModalProps> = ({
    isOpen,
    onClose,
    product,
    onSubmit,
}) => {
    const [bidAmount, setBidAmount] = useState<string>("");
    const [error, setError] = useState<string>("");

    useEffect(() => {
        if (!isOpen) {
            setBidAmount("");
            setError("");
        }
    }, [isOpen]);

    const handleBidSubmit = () => {
        if (!product) return;

        const currentBid = parseInt(product.currentBid.replace(/,/g, ""));
        const newBid = parseInt(bidAmount.replace(/,/g, ""));
        const minBidIncrement = 10000;

        if (!bidAmount || isNaN(newBid)) {
            setError("유효한 금액을 입력해주세요.");
            return;
        }

        if (newBid <= currentBid) {
            setError("현재 입찰가보다 높은 금액을 입력해주세요.");
            return;
        }

        if (newBid - currentBid < minBidIncrement) {
            setError(`최소 입찰 단위는 ${minBidIncrement.toLocaleString()}원입니다.`);
            return;
        }

        // 유효성 검사를 통과하면 부모에게 입찰 금액을 전달합니다.
        onSubmit(newBid);
    };

    if (!isOpen || !product) {
        return null;
    }

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-xl p-6 w-full max-w-md">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-xl font-bold">
                        입찰하기
                    </h3>
                    <button
                        onClick={onClose}
                        className="text-gray-500 hover:text-gray-700">
                        <i className="fas fa-times" />
                    </button>
                </div>
                <Fragment>
                    <div className="mb-4">
                        <p className="text-gray-600 mb-2">상품명</p>
                        <p className="font-medium">{product.name}</p>
                    </div>
                    <div className="mb-4">
                        <p className="text-gray-600 mb-2">현재 최고 입찰가</p>
                        <p className="font-medium text-blue-600">₩{product.currentBid}</p>
                    </div>
                    <div className="mb-4">
                        <p className="text-gray-600 mb-2">최소 입찰 단위</p>
                        <p className="font-medium">₩10,000</p>
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-600 mb-2">입찰 금액</label>
                        <div className="relative">
                            <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                                ₩
                            </span>
                            <input
                                type="text"
                                value={bidAmount}
                                onChange={(e) => {
                                    const value = e.target.value.replace(/[^0-9]/g, "");
                                    if (value === "" || /^\d+$/.test(value)) {
                                        setBidAmount(
                                            value.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
                                        );
                                    }
                                }}
                                className="w-full pl-8 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="금액을 입력하세요"
                            />
                        </div>
                        {error && (
                            <p className="text-red-500 text-sm mt-1">{error}</p>
                        )}
                    </div>
                    <button
                        onClick={handleBidSubmit}
                        className="w-full px-4 py-2 bg-blue-500 text-white !rounded-button hover:bg-blue-600 whitespace-nowrap"
                    >
                        입찰 확인
                    </button>
                </Fragment>
            </div>
        </div>
    );
};

export default BrandModal;