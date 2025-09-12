import {apiFetch} from "../../../utils/apiFetch";
import type {Wish} from '../types/Wish';
import type {ApiResponse} from "../../user/types/AuthTypes";

export const getWishes = async (): Promise<ApiResponse<Wish[]>> => {
    return apiFetch<ApiResponse<Wish[]>>('/api/wishes');
};

export const addWish = async (productId: number): Promise<ApiResponse<void>> => {
    return apiFetch<ApiResponse<void>>(`/api/wishes/${productId}`, {method: 'POST'});
};

export const removeWish = async (productId: number): Promise<ApiResponse<void>> => {
    return apiFetch<ApiResponse<void>>(`/api/wishes/${productId}`, {method: 'DELETE'});
};
