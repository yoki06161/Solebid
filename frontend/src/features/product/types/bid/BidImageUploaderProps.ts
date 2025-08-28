export interface BidImageUploaderProps {
    previewUrls: string[];
    selectedFiles: File[];
    onFilesChange: (newFiles: File[]) => void;
}