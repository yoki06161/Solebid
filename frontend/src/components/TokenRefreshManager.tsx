import { useEffect, useRef, useCallback } from 'react';

/**
 * 전역 액세스 토큰 선제 갱신 매니저
 * - 주기적으로 /api/auth/status를 확인하여 만료 임박 시 /api/auth/refresh 호출
 * - 포커스/가시성 변경 시 즉시 확인
 */
const TokenRefreshManager: React.FC = () => {
  const pollingIntervalRef = useRef<number | null>(null);
  const scheduledTimeoutRef = useRef<number | null>(null);
  const isRefreshingRef = useRef(false);
  const lastRefreshAtRef = useRef<number>(0);

  const clearTimers = () => {
    if (pollingIntervalRef.current) {
      window.clearInterval(pollingIntervalRef.current);
      pollingIntervalRef.current = null;
    }
    if (scheduledTimeoutRef.current) {
      window.clearTimeout(scheduledTimeoutRef.current);
      scheduledTimeoutRef.current = null;
    }
  };

  const scheduleNextByExpires = (expiresInSec: number) => {
    // 만료 60초 전에 재확인하도록 스케줄
    const leadSec = 60;
    const delayMs = Math.max(1_000, (expiresInSec - leadSec) * 1000);
    if (scheduledTimeoutRef.current) {
      window.clearTimeout(scheduledTimeoutRef.current);
    }
    scheduledTimeoutRef.current = window.setTimeout(() => {
      void checkAndMaybeRefresh();
    }, delayMs);
  };

  const doRefresh = useCallback(async () => {
    if (isRefreshingRef.current) return;
    isRefreshingRef.current = true;
    try {
      const now = Date.now();
      if (now - lastRefreshAtRef.current < 5_000) {
        return; // 과도한 연속 갱신 방지
      }
      const res = await fetch('/api/auth/refresh', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' }
      });
      const data = await res.json().catch(() => ({}));
      if (res.ok && data?.success) {
        lastRefreshAtRef.current = Date.now();
        const accessExp: number = Number(data?.data?.accessTokenExpiresIn ?? 0);
        if (accessExp > 0) scheduleNextByExpires(accessExp);
      } else {
        // 401 등인 경우: 다음 주기까지 대기 (명시적 로그아웃은 여기서 수행하지 않음)
      }
    } catch {
      // 네트워크 오류 무시
    } finally {
      isRefreshingRef.current = false;
    }
  }, []);

  const checkAndMaybeRefresh = useCallback(async () => {
    try {
      const res = await fetch('/api/auth/status', { credentials: 'include' });
      const payload = await res.json().catch(() => ({}));
      if (!res.ok || !payload?.success) return;

      const isAuthenticated: boolean = Boolean(payload?.data?.isAuthenticated);
      const expiresIn: number = Number(payload?.data?.accessTokenExpiresIn ?? 0);
      const refreshAvailable: boolean = Boolean(payload?.data?.refreshAvailable);

      if (!isAuthenticated) {
        // 인증 안 된 상태라도 refresh 쿠키가 있으면 즉시 회복 시도
        if (refreshAvailable) {
          await doRefresh();
        }
        return;
      }

      // 만료 임계치: 60초 이하면 선제 갱신
      const thresholdSec = 60;
      if (refreshAvailable && expiresIn > 0) {
        scheduleNextByExpires(expiresIn); // 다음 스케줄 갱신
        if (expiresIn <= thresholdSec) {
          await doRefresh();
        }
      }
    } catch {
      // 무시하고 다음 주기에서 재시도
    }
  }, [doRefresh]);

  useEffect(() => {
    // 즉시 1회 점검
    void checkAndMaybeRefresh();

    // 폴링: 30초마다 상태 확인
    pollingIntervalRef.current = window.setInterval(() => {
      void checkAndMaybeRefresh();
    }, 30_000);

    const handleVisChange = () => {
      if (document.visibilityState === 'visible') {
        void checkAndMaybeRefresh();
      }
    };
    const handleFocus = () => void checkAndMaybeRefresh();

    document.addEventListener('visibilitychange', handleVisChange);
    window.addEventListener('focus', handleFocus);

    return () => {
      clearTimers();
      document.removeEventListener('visibilitychange', handleVisChange);
      window.removeEventListener('focus', handleFocus);
    };
  }, [checkAndMaybeRefresh]);

  return null; // 렌더링 없음, 사이드 이펙트 전용
};

export default TokenRefreshManager;
