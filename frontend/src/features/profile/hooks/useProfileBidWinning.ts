import { useEffect, useState } from "react";
import { fetchProfileBidWinning } from "../services/ProfileBidService";
import type { ProfileBidWinningProps } from "../types/ProfileBidWinningProps";

export const useProfileBidWinning = () => {
    const [winningBids, setWinningBids] = useState<ProfileBidWinningProps[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchWinningBids = async () => {
            try {
                setLoading(true);
                const data = await fetchProfileBidWinning();

                if (Array.isArray(data)) {
                    setWinningBids(data);
                } else {
                    setWinningBids([]);
                }
                setError(null);
            } catch (err) {
                console.error('낙찰 내역 조회 실패:', err);
                setError('낙찰 내역을 불러오는데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchWinningBids();
    }, []);

    return { winningBids, loading, error };
};