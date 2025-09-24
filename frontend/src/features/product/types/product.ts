export type Brand = "NIKE" | "ADIDAS" | "NB" | "CONVERSE" | "VANS" | "PUMA" | "REEBOK" | "ASICS";
export type Category = "SNEAKERS" | "RUNNING" | "BASKETBALL" | "CANVAS";
export type Condition = "NEW" | "USED";
export type Status = "AVAILABLE" | "SOLD_OUT" | "CANCELLED";

export interface Product {
    id: string | number;
    name: string;
    brand: Brand;
    category: Category;
    price?: number;
    image?: string;
}

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
