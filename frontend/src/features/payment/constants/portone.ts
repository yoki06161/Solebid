// ===== values =====
export const PORTONE_MERCHANT_ID = import.meta.env.VITE_PORTONE_IMP_KEY ?? "";
export const API_HOST = import.meta.env.VITE_API_HOST ?? "";
export const PORTONE_SDK_URL = "https://cdn.iamport.kr/js/iamport.payment-1.2.0.js";
export const DEV_FAKE_PAY =
    import.meta.env.DEV && import.meta.env.VITE_DEV_FAKE_PAY === "1";

/** 절대 경로가 설정되어 있으면 합치고, 아니면 상대경로 그대로 사용 */
export const api = (path: string) => (API_HOST ? `${API_HOST}${path}` : path);

// ===== types (⬅️ 여기 새로 추가) =====
export type PortOnePayMethod = "card" | "trans" | "vbank";

export interface PortOnePayRequest {
    pg?: string;
    pay_method: PortOnePayMethod;
    merchant_uid: string;
    name?: string;
    amount: number;
    buyer_email?: string;
    buyer_name?: string;
    buyer_tel?: string;
}

export interface PortOnePayResponse {
    success: boolean;
    imp_uid?: string;
    merchant_uid?: string;
    error_msg?: string;
}

export interface PortOneIMP {
    init: (merchantId: string) => void;
    request_pay: (
        params: PortOnePayRequest,
        callback: (rsp: PortOnePayResponse) => void
    ) => void;
}

// 전역 window 보강 (파일이 이미 모듈이므로 export {} 불필요)
declare global {
    interface Window {
        IMP?: PortOneIMP;
    }
}
