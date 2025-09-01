export interface PointSummaryResponse {
    userId: number;
    currentPoint: number;
    updatedAt: string | null;
}

export interface ApiErrorResponse {
    success: false;
    data: null;
    errorCode: string;
    message: string;
}
