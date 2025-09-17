// src/utils/http.ts
// 공용 HTTP 유틸 — JWT 쿠키 기반으로 통일

export type ErrorResponse = {
    errorCode?: string;
    message?: string;
};

export const ERROR_MAP: Record<string, string> = {
    THUMBNAIL_DUPLICATED: "썸네일은 1장만 설정할 수 있습니다.",
    SORT_ORDER_DUPLICATED: "이미지 정렬 번호가 중복되었습니다.",
    INVALID_IMAGE_KEY: "이미지 경로가 올바르지 않습니다. (products/tmp/...)",
    INVALID_INPUT_VALUE: "입력값을 확인해 주세요.",
    INVALID_SIZE_RANGE: "잘못된 사이즈 범위입니다.",
    UNAUTHORIZED: "로그인이 필요합니다.",
    PRODUCT_NOT_FOUND: "상품을 찾을 수 없습니다.",
    AUCTION_ALREADY_EXISTS: "이미 진행(또는 대기) 중인 경매가 있습니다.",
    END_AT_MUST_BE_AFTER_START_AT: "종료일은 시작일 이후여야 합니다.",
    INTERNAL_SERVER_ERROR: "서버 내부 오류가 발생했습니다.",
};

export async function safeJson<T>(r: Response): Promise<T | null> {
    try {
        const t = await r.text();
        if (!t) return null;
        return JSON.parse(t) as T;
    } catch {
        return null;
    }
}

export function throwHttpError(body: ErrorResponse | null, fallback: string): never {
    const msg = body?.message || (body?.errorCode && ERROR_MAP[body.errorCode]) || fallback;
    throw new Error(msg);
}

/** 선택용: 필요 시만 Authorization 헤더를 쓰고, 기본은 쿠키로 인증 */
let AUTH_TOKEN: string | null = null;
export function setAuthToken(token: string | null) {
    AUTH_TOKEN = token;
}

type ApiOptions = {
    method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
    headers?: HeadersInit;
    /** 명시적으로 토큰을 넘길 때만 Authorization 헤더를 추가합니다. */
    token?: string | null;
    body?: unknown;
    signal?: AbortSignal;
};

export async function apiFetch<T>(url: string, opts: ApiOptions = {}): Promise<T> {
    const { method = "GET", body, headers: extra, token, signal } = opts;

    const baseHeaders: Record<string, string> = {
        Accept: "application/json",
    };
    if (typeof body === "object" && body !== null && !(body instanceof FormData)) {
        baseHeaders["Content-Type"] = "application/json";
    }
    //기본은 쿠키 인증. 토큰이 명시되면 Authorization도 추가.
    if (token ?? AUTH_TOKEN) {
        baseHeaders["Authorization"] = `Bearer ${token ?? AUTH_TOKEN}`;
    }

    const headers: HeadersInit = { ...baseHeaders, ...(extra ?? {}) };

    const init: RequestInit = {
        method,
        headers,
        credentials: "include",
        body:
            typeof body === "string"
                ? body
                : typeof body === "object" && body !== null && !(body instanceof FormData)
                    ? JSON.stringify(body)
                    : (body as BodyInit | undefined),
        signal,
    };

    const res = await fetch(url, init);
    const data = await safeJson<T | ErrorResponse>(res);

    if (!res.ok) {
        throwHttpError((data as ErrorResponse) ?? null, "요청에 실패했습니다.");
    }

    // 204/빈 본문도 안전 처리
    // eslint-disable-next-line @typescript-eslint/consistent-type-assertions
    return (data ?? (undefined as unknown as T)) as T;
}

export const http = {
    get<T>(url: string, opts?: Omit<ApiOptions, "method" | "body">) {
        return apiFetch<T>(url, { ...opts, method: "GET" });
    },
    post<T>(url: string, body?: unknown, opts?: Omit<ApiOptions, "method">) {
        return apiFetch<T>(url, { ...opts, method: "POST", body });
    },
    put<T>(url: string, body?: unknown, opts?: Omit<ApiOptions, "method">) {
        return apiFetch<T>(url, { ...opts, method: "PUT", body });
    },
    patch<T>(url: string, body?: unknown, opts?: Omit<ApiOptions, "method">) {
        return apiFetch<T>(url, { ...opts, method: "PATCH", body });
    },
    delete<T>(url: string, opts?: Omit<ApiOptions, "method" | "body">) {
        return apiFetch<T>(url, { ...opts, method: "DELETE" });
    },
};
