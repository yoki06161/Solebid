import {
    PORTONE_MERCHANT_ID,
    PORTONE_SDK_URL,
    type PortOneIMP,
    type PortOnePayMethod,
    type PortOnePayRequest,
    type PortOnePayResponse,
} from "../constants/portone";

const isObj = (v: unknown): v is Record<string, unknown> => typeof v === "object" && v !== null;
const isFn  = (v: unknown): v is (...a: unknown[]) => unknown => typeof v === "function";
const isIMP = (v: unknown): v is PortOneIMP => isObj(v) && isFn((v as any).init) && isFn((v as any).request_pay);
const isPayResp = (v: unknown): v is PortOnePayResponse => isObj(v) && typeof (v as any).success === "boolean";

async function loadSDK(): Promise<PortOneIMP> {
    if (isIMP(window.IMP)) return window.IMP!;
    if (!document.getElementById("portone-sdk")) {
        const s = document.createElement("script");
        s.id = "portone-sdk"; s.src = PORTONE_SDK_URL; s.async = true;
        document.head.appendChild(s);
    }
    return await new Promise<PortOneIMP>((resolve, reject) => {
        const start = Date.now();
        const t = setInterval(() => {
            if (isIMP(window.IMP)) { clearInterval(t); resolve(window.IMP!); }
            if (Date.now() - start > 8000) { clearInterval(t); reject(new Error("PortOne SDK 로드 타임아웃")); }
        }, 100);
    });
}

/**
 * 백엔드 계약:
 * - POST /api/payments/charge/prepare  -> { orderId, redirectUrl }
 * - GET  /api/portone/approve?impUid=...
 */
export async function startPortoneCharge(params: {
    amount: number;                       // UI 금액
    payMethod?: PortOnePayMethod;         // "card" | "trans" | "vbank"
    redirectUrl?: string;                 // (사용 안 함) 서버 리다이렉트 방지 목적
    buyer?: { email?: string; name?: string; tel?: string };
    pg?: string;                          // default "html5_inicis"
    title?: string;                       // default "포인트 충전"
}): Promise<{ impUid?: string; merchantUid?: string; orderId: string }> {
    const {
        amount,
        payMethod = "card",
        // redirectUrl 는 더 이상 사용하지 않음 (리다이렉트 방지)
        buyer = {},
        pg = "html5_inicis",
        title = "포인트 충전",
    } = params;

    if (!PORTONE_MERCHANT_ID) throw new Error("가맹점 식별코드가 설정되지 않았습니다.");

    // 1) 서버 준비 호출 (⚠️ redirectUrl 전달 제거)
    const prepareRes = await fetch("/api/payments/charge/prepare", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            amount,
            paymentMethod: payMethod === "trans" ? "TRANS" : payMethod === "vbank" ? "VBANK" : "CARD",
            // redirectUrl: 제거 → 서버가 PG 리다이렉트를 설정하지 않도록
        }),
    });
    if (!prepareRes.ok) throw new Error(`prepare 실패: ${await prepareRes.text()}`);
    const prep = (await prepareRes.json()) as { orderId: string; redirectUrl?: string };
    const orderId = prep.orderId;
    // const mRedirect = prep.redirectUrl; // 사용 안 함

    // 2) PortOne 결제창 (카드/계좌이체만)
    if (payMethod === "card" || payMethod === "trans") {
        const IMP = await loadSDK();
        IMP.init(PORTONE_MERCHANT_ID);

        const payParams: PortOnePayRequest = {
            pg,                               // 반드시 "html5_inicis"
            pay_method: payMethod,            // "card" | "trans"
            merchant_uid: orderId,            // 서버 생성 orderId 사용
            name: title,
            amount,
            ...(buyer.email ? { buyer_email: buyer.email } : {}),
            ...(buyer.name ? { buyer_name: buyer.name } : {}),
            ...(buyer.tel ? { buyer_tel: buyer.tel } : {}),
            // m_redirect_url: mRedirect,     // ❌ 절대 넣지 않음 (외부 리다이렉트 방지)
        };

        const rsp = await new Promise<PortOnePayResponse>((resolve, reject) => {
            IMP.request_pay(payParams, (cb: unknown) => {
                if (!isPayResp(cb)) { reject(new Error("결제 콜백 응답 형식 오류")); return; }
                resolve(cb as PortOnePayResponse);
            });
        });

        if (!rsp.success || !rsp.imp_uid) {
            throw new Error(rsp.error_msg || "결제가 취소되었거나 실패했습니다.");
        }

        // 3) 서버 승인 처리 (GET /api/portone/approve?impUid=...)
        const approveRes = await fetch(`/api/portone/approve?impUid=${encodeURIComponent(rsp.imp_uid)}`);
        if (!approveRes.ok) throw new Error(`approve 실패: ${await approveRes.text()}`);

        return { impUid: rsp.imp_uid, merchantUid: rsp.merchant_uid, orderId };
    }

    // vbank 등 서버 주도라면 여기서 종료
    return { orderId };
}
