export interface AuctionModalProps {
    isOpen: boolean;
    onClose: () => void;
    currentBid: string;
    onSubmit: (bidAmount: number) => void;
}