// 공용 HTTP 유틸 + 임시 인증 (X-User-Id) -> 나중에 JWT로 교체

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

/** 인증 상태: 지금은 X-User-Id, 추후 JWT로 전환 예정 */
let AUTH_TOKEN: string | null = null;
let TEST_USER_ID: number | null = 1;

export function setAuthToken(token: string | null) {
    AUTH_TOKEN = token;
}
export function setTestUserId(userId: number | null) {
    TEST_USER_ID = userId;
}

function buildAuthHeaders(overrides?: { token?: string; userId?: number }): Record<string, string> {
    const token = overrides?.token ?? AUTH_TOKEN;
    if (token) {
        return { Authorization: `Bearer ${token}` };
    }
    const userId = overrides?.userId ?? TEST_USER_ID;
    return userId ? { "X-User-Id": String(userId) } : {};
}

type ApiOptions = {
    method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
    headers?: HeadersInit;
    token?: string | null;
    userId?: number | null;
    body?: unknown;
    signal?: AbortSignal;
};

export async function apiFetch<T>(url: string, opts: ApiOptions = {}): Promise<T> {
    const { method = "GET", body, headers: extra, token, userId, signal } = opts;

    // ⚠️ TS2322 방지: object를 먼저 만든 뒤 조건부로 key 설정
    const baseHeaders: Record<string, string> = {};
    if (typeof body === "object" && body !== null && !(body instanceof FormData)) {
        baseHeaders["Content-Type"] = "application/json";
    }
    const auth = buildAuthHeaders({ token: token ?? undefined, userId: userId ?? undefined });
    const headers: HeadersInit = { ...baseHeaders, ...auth, ...(extra ?? {}) };

    const init: RequestInit = {
        method,
        headers,
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
