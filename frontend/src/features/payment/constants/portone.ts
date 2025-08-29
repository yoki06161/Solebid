export const PORTONE_SDK_URL = "https://cdn.iamport.kr/v1/iamport.js";
export const PORTONE_MERCHANT_ID = "imp07364387"; // ← 실제 imp 코드

export type PortOnePayMethod = "card" | "trans" | "vbank";
export type PortOnePayRequest = {
    pg?: string;
    pay_method: PortOnePayMethod;
    merchant_uid: string;
    name?: string;
    amount: number;
    buyer_email?: string;
    buyer_name?: string;
    buyer_tel?: string;
    m_redirect_url?: string;
};
export type PortOnePayResponse = {
    success: boolean;
    imp_uid?: string;
    merchant_uid?: string;
    paid_amount?: number;
    error_msg?: string;
};
export type PortOneIMP = {
    init(code: string): void;
    request_pay(params: PortOnePayRequest, cb: (rsp: PortOnePayResponse) => void): void;
};
