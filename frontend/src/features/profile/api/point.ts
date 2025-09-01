import type { PointSummaryResponse, ApiErrorResponse } from "../types/ProfilePointProps";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

export async function fetchUserPoint(userId: number): Promise<PointSummaryResponse> {

    const token = localStorage.getItem("accessToken"); // 또는 Context/Recoil에서 관리

    const res = await fetch(`${BASE_URL}/users/${userId}/points`, {
        method: "GET",
        headers: {
            "Accept": "application/json",
            "Authorization": `Bearer ${token}`,   //  JWT 붙이기
        },
    });

    if (!res.ok) {
        let msg = `HTTP ${res.status}`;
        try {
            const data: unknown = await res.json();
            const err = data as Partial<ApiErrorResponse>;
            if (err.errorCode && err.message) {
                msg = `${err.errorCode}: ${err.message}`;
            }
        } catch (e) {
            console.error("Error parsing error response:", e);
        }
        throw new Error(msg);
    }

    return res.json() as Promise<PointSummaryResponse>;
}
