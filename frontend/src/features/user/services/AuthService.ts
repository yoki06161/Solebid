import type { ApiResponse, AuthUser, OAuth2AuthUrl, Provider, TokenStatus } from '../types/AuthTypes.ts';

const jsonHeaders = { 'Content-Type': 'application/json' } as const;

export async function getOAuth2AuthUrl(provider: Provider): Promise<ApiResponse<OAuth2AuthUrl>> {
  const res = await fetch(`/api/auth/oauth2/${provider}/url`, { credentials: 'include' });
  const data: ApiResponse<OAuth2AuthUrl> = await res.json();
  return data;
}

export async function postOAuth2Callback(provider: string, code: string, state: string): Promise<ApiResponse<AuthUser>> {
  const res = await fetch(`/api/auth/oauth2/${provider}/callback`, {
    method: 'POST',
    headers: jsonHeaders,
    credentials: 'include',
    body: JSON.stringify({ code, state }),
  });
  const data: ApiResponse<AuthUser> = await res.json();
  return data;
}

export async function refreshToken(): Promise<ApiResponse<{ accessTokenExpiresIn: number; refreshTokenExpiresIn: number }>> {
  const res = await fetch('/api/auth/refresh', { method: 'POST', credentials: 'include' });
  const data = await res.json();
  return data;
}

export async function tokenStatus(): Promise<ApiResponse<TokenStatus>> {
  const res = await fetch('/api/auth/status', { credentials: 'include' });
  const data = await res.json();
  return data;
}

export async function logout(): Promise<ApiResponse> {
  const res = await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
  const data = await res.json();
  return data;
}

export function cacheUserAndEmit(user: AuthUser | null | undefined) {
  try {
    if (user) {
      sessionStorage.setItem('auth.user', JSON.stringify(user));
    } else {
      sessionStorage.removeItem('auth.user');
    }
  } catch {}
  try {
    const evt = new CustomEvent('auth-changed', { detail: { user: user || null } });
    window.dispatchEvent(evt);
  } catch {}
}

