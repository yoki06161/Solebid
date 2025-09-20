import { apiFetch } from "../../../utils/apiFetch.ts";

interface PresignedUrlResponse {
    [key: string]: string;
}

export const getPresignedUrls = async (imageKeys: string[]): Promise<PresignedUrlResponse> => {
    if (imageKeys.length === 0) {
        return {};
    }

    return await apiFetch<PresignedUrlResponse>('/api/uploads/download-urls', {
        method: 'POST',
        json: { imageKeys },
    });
};
