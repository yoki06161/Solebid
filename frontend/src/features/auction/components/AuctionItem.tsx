import {format} from "date-fns";
import React from "react";
import {useWishes} from "../../wish/hooks/useWishes.ts";
import type {AuctionItemProps} from "../types/AuctionItemProps";

const AuctionItem = ({item, onBidClick}: AuctionItemProps) => {
    const date = new Date(item.timeLeft);

    const timeLeft = format(date, 'HH:mm:ss');

    const formatter = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
    });

    const currentBid = formatter.format(item.currentBid ?? 0);

    const {isWished, addWish, removeWish, isAdding, isRemoving} = useWishes();

    const wished = isWished(item.id);

    const isLoading = isAdding || isRemoving;

    const handleClick = (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();

        if (isLoading) return;

        if (wished) {
            removeWish(item.id);
        } else {
            addWish(item);
        }
    };

    return (
        <div className="bg-white rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow">
            <div className="relative h-64">
                <img
                    src={item.image || 'https://via.placeholder.com/300'}
                    alt={item.name}
                    className="w-full h-full object-cover"
                />
                <button
                    onClick={handleClick}
                    className="absolute top-2 right-2 w-8 h-8 bg-white rounded-full flex items-center justify-center shadow-md hover:bg-red-50 cursor-pointer"
                >
                    <i className={`fas fa-heart text-sm ${wished ? 'text-red-500' : 'text-gray-400'}`}/>
                </button>
            </div>
            <div className="p-6">
                <div className="text-sm text-gray-500 mb-1">
                    {item.brand}
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-4">
                    {item.name}
                </h3>
                <div className="flex justify-between items-center mb-2">
                    <span className="text-sm text-gray-600">
                        현재 입찰가
                    </span>
                    <span className="text-lg font-semibold text-blue-600">
                        {currentBid}
                    </span>
                </div>
                <div className="flex justify-between items-center mb-4">
                    <span className="text-sm text-gray-600">
                        남은 시간
                    </span>
                    <span className="text-sm font-medium text-red-500">
                        {timeLeft}
                    </span>
                </div>
                <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-500">
                        {item.bidders}명 참여
                    </span>
                    <button
                        onClick={() => onBidClick(item)}
                        className="ml-2 px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 whitespace-nowrap cursor-pointer"
                    >
                        입찰하기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AuctionItem;