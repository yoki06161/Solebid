export type PresignResponse = {
    key: string;      // products/tmp/xxx.jpg
    putUrl: string;   // S3 presigned PUT URL
    publicUrl?: string;
};
