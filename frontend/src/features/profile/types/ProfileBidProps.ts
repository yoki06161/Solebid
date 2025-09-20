export interface ProfileBidProps {
    bidId: number;
    productId: number;
    productName: string;
    productImageUrl: string | null;
    winningAmount: number;
    bidTime: string;
    productBrand: string;
    productCategory: string;
    productSize: number;
    resolvedImageUrl?: string; // presigned URL 또는 기본 이미지 URL
}
