import { apiFetch } from "../../../utils/apiFetch";
import type { ApiResponse } from "../../user/types/AuthTypes";
import type { ProfileBidSellingProps } from "../types/ProfileBidSellingProps";
import type { ProfileBidWinningProps } from "../types/ProfileBidWinningProps";

export const fetchProfileBidWinning = async (): Promise<ProfileBidWinningProps[]> => {
    const response = await apiFetch<ApiResponse<ProfileBidWinningProps[]>>('/api/bids/winning');

    if (!response.success) {
        throw new Error(response.message || '낙찰 내역 조회에 실패했습니다.');
    }

    return response.data || [];
};

export const fetchProfileBidSelling = async (): Promise<ProfileBidSellingProps[]> => {
    const response = await apiFetch<ApiResponse<ProfileBidSellingProps[]>>('/api/bids/selling');

    if (!response.success) {
        throw new Error(response.message || '판매 내역 조회에 실패했습니다.');
    }

    return response.data || [];
};
