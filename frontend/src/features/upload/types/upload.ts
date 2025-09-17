export interface PresignRequest {
    fileName: string;
    contentType?: string | null;
}

export type PresignResponse = {
    key: string;      // products/tmp/xxx.jpg
    putUrl: string;   // S3 presigned PUT URL
    publicUrl?: string;
};
