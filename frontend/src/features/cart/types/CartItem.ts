export interface CartItem {
    cartId: number;
    productId: number;
    productName: string;
    productImageUrl: string | null; // 백엔드에서 받은 원본 S3 키
    productPrice: number;
    productBrand: string;
    productCategory: string;
    productSize: number;
    quantity: number;
    imageUrl?: string; // useImageUrls 훅에서 추가되는 presigned URL 
}
