import { useEffect, useState } from "react";
import { fetchUserPoint } from "../api/point";
import type { PointSummaryResponse } from "../types/ProfilePointProps";
import { useAuth } from "../../user/hooks/useAuth";

const ProfilePoint = () => {
    const { user } = useAuth();
    const [point, setPoint] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const userId = user?.userId;

    useEffect(() => {
        if (!userId) {
            setError("로그인 후 이용 가능합니다.");
            setLoading(false);
            return;
        }
        let alive = true;
        setLoading(true);

        fetchUserPoint(userId)
            .then((res: PointSummaryResponse) => {
                if (alive) {
                    setPoint(res.currentPoint);
                    setError(null);
                }
            })
            .catch((err: Error) => {
                if (alive) setError(err.message);
            })
            .finally(() => {
                if (alive) setLoading(false);
            });

        return () => {
            alive = false;
        };
    }, [userId]);

    const handleGoToCharge = () => {
        // 리액트 라우터 방식 이동 원하면:
        // navigate("/points/charge");
        // 강제 페이지 이동 원하면:
        window.location.href = "http://localhost:5173/points/charge";
    };

    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">SolePay</h3>
            <div className="space-y-4">
                <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
                    <div>
                        <div className="font-medium text-gray-900">적립 포인트</div>
                        {loading ? (
                            <div className="text-gray-400">불러오는 중...</div>
                        ) : error ? (
                            <div className="text-red-500">{error}</div>
                        ) : (
                            <div className="text-blue-600 font-semibold">
                                {point?.toLocaleString()}P
                            </div>
                        )}
                    </div>
                    <i className="fas fa-coins text-blue-600 text-xl" />
                </div>

                <button
                    onClick={handleGoToCharge}
                    className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 !rounded-button whitespace-nowrap"
                >
                    <i className="fas fa-exchange-alt mr-2" />
                    SolePay 전환
                </button>
            </div>
        </div>
    );
};

export default ProfilePoint;
