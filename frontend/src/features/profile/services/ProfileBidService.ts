import { apiFetch } from "../../../utils/apiFetch";
import type { ApiResponse } from "../../user/types/AuthTypes";
import type { ProfileBidProps } from "../types/ProfileBidProps";

export const fetchProfileBidWinning = async (): Promise<ProfileBidProps[]> => {
    const response = await apiFetch<ApiResponse<ProfileBidProps[]>>('/api/bids/winning');

    if (!response.success) {
        throw new Error(response.message || '낙찰 내역 조회에 실패했습니다.');
    }

    return response.data || [];
};
