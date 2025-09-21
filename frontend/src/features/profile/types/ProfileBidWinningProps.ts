export interface ProfileBidWinningProps {
    bidId: number;
    productId: number;
    productName: string;
    productImageUrl: string | null; // 백엔드에서 받은 원본 S3 키
    winningAmount: number;
    bidTime: string;
    productBrand: string;
    productCategory: string;
    productSize: number;
    imageUrl?: string; // useImageUrls 훅에서 추가되는 presigned URL 
}
