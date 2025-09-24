import React, { useEffect, useMemo, useState } from "react";
import type { AuctionItemProps } from "../types/AuctionItemProps";
import { S3_BASE_URL } from '../../../constants/config';

function formatRemaining(ms: number) {
    if (ms <= 0) return '경매 종료';
    const totalSeconds = Math.floor(ms / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    return [hours, minutes, seconds]
        .map((unit) => String(unit).padStart(2, '0'))
        .join(':');
}

const AuctionItem = ({ item, addWish, removeWish, isAdding, isRemoving, onBidClick, onSelect }: AuctionItemProps) => {
    const computeRemaining = useMemo(() => {
        const endTime = new Date(item.timeLeft).getTime();
        if (Number.isNaN(endTime)) return () => '-';
        return () => formatRemaining(endTime - Date.now());
    }, [item.timeLeft]);

    const [remainingLabel, setRemainingLabel] = useState(computeRemaining());

    useEffect(() => {
        setRemainingLabel(computeRemaining());
        const id = window.setInterval(() => {
            setRemainingLabel(computeRemaining());
        }, 1000);
        return () => window.clearInterval(id);
    }, [computeRemaining]);

    const formatter = new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
    });

    const currentBid = formatter.format(item.currentBid ?? 0);

    const isLoading = isAdding || isRemoving;

    const handleClick = (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();

        if (isLoading) return;

        if (item.isWished) {
            removeWish(item.id);
        } else {
            addWish(item);
        }
    };

    return (
        <div
            className="bg-white rounded-lg shadow-sm overflow-hidden hover:shadow-md transition-shadow cursor-pointer"
            onClick={() => onSelect(item)}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    onSelect(item);
                }
            }}
        >
            <div className="relative h-64">
                <img
                    src={item.imageUrl || `${S3_BASE_URL}/${item.image}`}
                    alt={item.name}
                    className="w-full h-full object-cover"
                />
                <button
                    onClick={(e) => {
                        e.stopPropagation();
                        handleClick(e);
                    }}
                    className="absolute top-2 right-2 w-8 h-8 bg-white rounded-full flex items-center justify-center shadow-md hover:bg-red-50 cursor-pointer"
                >
                    <i className={
                        `fas fa-heart text-sm 
                        ${item.isWished
                            ? 'text-red-500'
                            : 'text-gray-400'
                        }`}
                    />
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
                        {remainingLabel}
                    </span>
                </div>
                <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-500">
                        {item.bidders}명 참여
                    </span>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onBidClick(item);
                        }}
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
