export interface AuctionCreateRequest {
    productId: number;
    startPrice: number;
    endAt: string;
}
export interface AuctionCreateResponse {
    id: number;
}
