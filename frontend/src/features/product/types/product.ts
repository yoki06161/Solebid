
/*
// 서버 enum과 1:1로 맞춘 문자열 유니온
export type Brand =
    | "NIKE" | "ADIDAS" | "NB" | "CONVERSE" | "VANS" | "PUMA" | "REEBOK" | "ASICS";

export type Category = "SNEAKERS" | "RUNNING" | "BASKETBALL" | "CANVAS";
export type Condition = "NEW" | "USED";
export type Status = "AVAILABLE" | "SOLD_OUT" | "CANCELLED";

// 생성 페이로드
export interface ProductCreatePayload {
    category: Category;
    status: Status;
    condition: Condition;
    brand: Brand;
    size: number;
    name: string;
    description: string;
    modelCode: string | null;
    colorway: string | null;
    releaseDate: string | null; // YYYY-MM-DD
    images: Array<{
        filePath: string;
        fileName: string;
        sortOrder: number;
        isThumbnail: boolean;
    }>;
}

export interface CreateProductResponse {
    productId: number;
}

export interface FinalizeImagesResponse {
    productId: number;
    finalized: boolean;
}

// 검색/목록 등에서 가볍게 쓸 프론트 전용 타입
export interface Product {
    id: string | number;
    name: string;
    brand: Brand;
    category: Category;
    price?: number;
    image?: string;
}
*/

// src/features/product/types/product.ts
export type Brand = "NIKE" | "ADIDAS" | "NB" | "CONVERSE" | "VANS" | "PUMA" | "REEBOK" | "ASICS";
export type Category = "SNEAKERS" | "RUNNING" | "BASKETBALL" | "CANVAS";
export type Condition = "NEW" | "USED";
export type Status = "AVAILABLE" | "SOLD_OUT" | "CANCELLED";

export interface ProductCreatePayload {
    category: Category;
    status: Status;
    condition: Condition;
    brand: Brand;
    size: number;
    name: string;
    description: string;
    modelCode: string | null;
    colorway: string | null;
    releaseDate: string | null;
    images: Array<{
        filePath: string;
        fileName: string;
        sortOrder: number;
        isThumbnail: boolean;
    }>;
}

export interface CreateProductResponse {
    productId: number;
}

export interface FinalizeImagesResponse {
    id: number;
    success: boolean;
}
