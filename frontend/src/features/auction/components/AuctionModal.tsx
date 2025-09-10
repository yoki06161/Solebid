import React, {useState} from "react";
import type {AuctionModalProps} from "../types/AuctionModalProps";

const AuctionModal = ({isOpen, onClose, currentBid, onSubmit}: AuctionModalProps) => {
    const [bidAmount, setBidAmount] = useState<string>('');

    // const currentBidNumber = parseInt(currentBid.replace(/,/g, ''), 10);
    const minBidIncrement = 10000;
    const minBidAmount = currentBid + minBidIncrement;

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const amount = parseInt(bidAmount, 10);
        if (!isNaN(amount) && amount >= minBidAmount) {
            onSubmit(amount);
            onClose();
        } else {
            alert(`최소 입찰가 ₩${minBidAmount.toLocaleString()} 이상이어야 합니다.`);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
                <h3 className="text-xl font-semibold mb-4">
                    입찰하기
                </h3>
                <div className="mb-4">
                    <p className="text-gray-600">
                        현재 입찰가: ₩{currentBid}
                    </p>
                    <p className="text-gray-600">
                        최소 입찰 단위: ₩{minBidIncrement.toLocaleString()}
                    </p>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-700 mb-2">
                            입찰 금액
                        </label>
                        <input
                            type="number"
                            min={minBidAmount}
                            step={minBidIncrement}
                            value={bidAmount}
                            onChange={(e) => setBidAmount(e.target.value)}
                            placeholder={`최소 입찰가: ₩${minBidAmount.toLocaleString()}`}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <div className="flex space-x-4">
                        <button
                            type="submit"
                            onClick={() => {}}
                            className="flex-1 px-4 py-2 bg-blue-500 text-white !rounded-button hover:bg-blue-600 whitespace-nowrap"
                        >
                            입찰하기
                        </button>
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 !rounded-button hover:bg-gray-50 whitespace-nowrap"
                        >
                            취소
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AuctionModal;