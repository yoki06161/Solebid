export interface AuctionModalProps {
    isOpen: boolean;
    onClose: () => void;
    currentBid: number;
    onSubmit: (bidAmount: number) => void;
}