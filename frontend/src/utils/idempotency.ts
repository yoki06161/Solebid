// 멱등 키
// 요청마다 고유한 UUID 생성, POST 요청 중복 방지용으로 사용.

export function idemKey(prefix = "req"): string {
    if ("crypto" in window && "randomUUID" in crypto) {
        return `${prefix}_${crypto.randomUUID()}`;
    }
    // fallback (구형 브라우저 대응)
    return `${prefix}_${Math.random().toString(36).slice(2)}_${Date.now()}`;
}

