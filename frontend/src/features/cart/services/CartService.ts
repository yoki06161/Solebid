import { apiFetch } from "../../../utils/apiFetch";
import type { ApiResponse } from "../../user/types/AuthTypes";
import type { CartItem } from "../types/CartItem";

export const fetchCartItems = async (): Promise<CartItem[]> => {
    const response = await apiFetch<ApiResponse<CartItem[]>>('/api/cart');

    if (!response.success) {
        throw new Error(response.message || '장바구니 조회에 실패했습니다.');
    }

    return response.data || [];
};