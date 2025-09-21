import { useEffect, useState } from "react";
import { fetchProfileBidSelling } from "../services/ProfileBidService";
import type { ProfileBidSellingProps } from "../types/ProfileBidSellingProps";

export const useProfileBidSelling = () => {
    const [soldProducts, setSoldProducts] = useState<ProfileBidSellingProps[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchSoldProducts = async () => {
            try {
                setLoading(true);
                const data = await fetchProfileBidSelling();

                if (Array.isArray(data)) {
                    setSoldProducts(data);
                } else {
                    setSoldProducts([]);
                }
                setError(null);
            } catch (err) {
                console.error('판매 내역 조회 실패:', err);
                setError('판매 내역을 불러오는데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchSoldProducts();
    }, []);

    return { soldProducts, loading, error };
};