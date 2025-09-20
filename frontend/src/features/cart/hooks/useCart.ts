import { useCallback, useEffect, useState, useMemo } from "react";
import { fetchCartItems } from "../services/CartService";
import { useImageUrls } from "../../../hooks/useProductImageUrls";
import type { CartItem } from "../types/CartItem";

export const useCart = () => {
    const [cartItems, setCartItems] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // 이미지 URL 처리를 위한 훅 - useCallback으로 함수 메모이제이션
    const cartItemImageExtractor = useCallback((item: CartItem): string | null => item.productImageUrl, []);
    const { itemsWithImages: cartItemsWithImages, isLoadingImages } = useImageUrls(cartItems, cartItemImageExtractor);

    useEffect(() => {
        const loadCartItems = async () => {
            try {
                setLoading(true);
                const data = await fetchCartItems();
                
                if (Array.isArray(data)) {
                    setCartItems(data);
                } else {
                    setCartItems([]);
                }
                setError(null);
            } catch (err) {
                console.error('장바구니 조회 실패:', err);
                setError('장바구니를 불러오는데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        loadCartItems();
    }, []);

    const removeItem = useCallback((cartId: number) => {
        setCartItems((items) => items.filter((item) => item.cartId !== cartId));
    }, []);

    return { 
        cartItems: cartItemsWithImages, 
        loading: loading || isLoadingImages, 
        error, 
        removeItem 
    };
};