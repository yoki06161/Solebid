import type { ProfileBidItemProps } from "../features/profile/types/ProfileBidItemProps";
import type { ProfileBidWinningProps } from "../features/profile/types/ProfileBidWinningProps";

export const formatDate = (bidTime: string): string => {
    return new Date(bidTime).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    }).replace(/\./g, '.').replace(/\s/g, '');
};

export const formatPrice = (amount: number): string => {
    return `${amount.toLocaleString()}원`;
};

export const convertToBidItemProps = (bid: ProfileBidWinningProps & { imageUrl?: string }): ProfileBidItemProps => {
    return {
        name: bid.productName,
        date: formatDate(bid.bidTime),
        price: formatPrice(bid.winningAmount),
        imageUrl: bid.imageUrl || '/placeholder-image.jpg'
    };
};