import { Fragment, useEffect, useState } from "react";
import { useModal } from "../../../contexts/modal/modal";
import { fetchUserPoint } from "../api/point";
import type { PointSummaryResponse } from "../types/ProfilePointProps";

type AuthUser = {
    userId?: number;
    email?: string;
    nickname?: string;
    [key: string]: unknown;
};

const ProfilePoint = () => {
    const { openModal, closeModal } = useModal();
    const [point, setPoint] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // 임시 하드코딩 : 실제 로그인된 유저 ID를 auth로 가져와야 함 (jwt 설정)
    //const userId = 1;+

    // 세션에 저장된 로그인 사용자 정보에서 userId 읽기
    const getCurrentUser = (): AuthUser | null => {
        try {
            const raw = sessionStorage.getItem("auth.user");
            if (!raw) return null;
            return JSON.parse(raw) as AuthUser;
        } catch {
            return null;
        }
    };

    const user = getCurrentUser();
    const userId = user?.userId; // null이면 로그인 안 된 상태

    useEffect(() => {
        if (!userId) {
            setError("로그인 후 이용 가능합니다.");
            setLoading(false);
            return;

        }
        let alive = true;
        setLoading(true);

        const token  = localStorage.getItem("accessToken")

        console.log(`test toekn: ${token}`)

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

    const handleOpenModal = () => {
        openModal(<PointConvertFormat onClose={closeModal} />);
    };

    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                SolePay
            </h3>
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
                    onClick={handleOpenModal}
                    className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 !rounded-button whitespace-nowrap"
                >
                    <i className="fas fa-exchange-alt mr-2" />
                    SolePay 전환
                </button>
            </div>
        </div>
    );
};

const PointConvertFormat = ({ onClose }: { onClose: () => void }) => {
    const handleConvert = () => {
        alert("포인트 전환이 완료되었습니다.");
        onClose();
    };
    return (
        <Fragment>
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                현금을 포인트로 전환
            </h3>
            <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    전환할 금액
                </label>
                <div className="relative">
                    <input
                        type="number"
                        placeholder="금액을 입력하세요"
                        min="1000"
                        step="1000"
                        className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <span className="absolute right-10 top-2 text-gray-500">원</span>
                </div>
                <p className="text-sm text-gray-500 mt-1">
                    최소 1,000원부터 전환 가능
                </p>
            </div>
            <div className="flex justify-end space-x-3">
                <button
                    onClick={onClose}
                    className="px-4 py-2 text-gray-600 hover:text-gray-900 !rounded-button whitespace-nowrap"
                >
                    취소
                </button>
                <button
                    onClick={handleConvert}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 !rounded-button whitespace-nowrap"
                >
                    전환하기
                </button>
            </div>
        </Fragment>
    );
};



/*
    useEffect(() => {
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
        return () => { alive = false; };
    }, [userId]);

    const handleOpenModal = () => {
        openModal(<PointConvertFormat onClose={closeModal} />);
    };

    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                SloePay
            </h3>
            <div className="space-y-4">
                <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
                    <div>
                        <div className="font-medium text-gray-900">
                             보유 포인트
                        </div>
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
                    onClick={handleOpenModal}
                    className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 !rounded-button whitespace-nowrap">
                    <i className="fas fa-exchange-alt mr-2" />
                    SloePay 충전하기
                </button>
            </div>
        </div>
    );
};

const PointConvertFormat = ({ onClose }: { onClose: () => void }) => {
    const handleConvert = () => {
        alert("포인트 전환이 완료되었습니다.");
        onClose();
    };
    return (
        <Fragment>
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                현금을 포인트로 전환
            </h3>
            <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    전환할 금액
                </label>
                <div className="relative">
                    <input
                        type="number"
                        placeholder="금액을 입력하세요"
                        min="1000"
                        step="1000"
                        className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <span className="absolute right-10 top-2 text-gray-500">
                        원
                    </span>
                </div>
                <p className="text-sm text-gray-500 mt-1">
                    최소 1,000원부터 전환 가능
                </p>
            </div>
            <div className="flex justify-end space-x-3">
                <button
                    onClick={onClose}
                    className="px-4 py-2 text-gray-600 hover:text-gray-900 !rounded-button whitespace-nowrap">
                    취소
                </button>
                <button
                    onClick={handleConvert}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 !rounded-button whitespace-nowrap">
                    전환하기
                </button>
            </div>
        </Fragment>
    );
};
*/

export default ProfilePoint;