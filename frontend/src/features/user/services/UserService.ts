import type { ApiResponse, AuthUser, LoginForm } from '../types/AuthTypes';

const jsonHeaders = { 'Content-Type': 'application/json' } as const;

export async function loginWithEmail(form: LoginForm): Promise<ApiResponse<AuthUser>> {
  const res = await fetch('/api/users/login', {
    method: 'POST',
    headers: jsonHeaders,
    credentials: 'include',
    body: JSON.stringify(form),
  });
  const data: ApiResponse<AuthUser> = await res.json();
  return data;
}

export async function reactivateAccount(token: string): Promise<ApiResponse<AuthUser>> {
  const res = await fetch('/api/users/reactivate', {
    method: 'POST',
    headers: jsonHeaders,
    credentials: 'include',
    body: JSON.stringify({ token }),
  });
  const data: ApiResponse<AuthUser> = await res.json();
  return data;
}

export async function checkNicknameAvailability(nickname: string): Promise<ApiResponse<{ available: boolean }>> {
  const res = await fetch(`/api/users/nickname/available?nickname=${encodeURIComponent(nickname)}`, {
    credentials: 'include',
  });
  const data = await res.json();
  return data;
}

export async function updateNickname(nickname: string): Promise<ApiResponse<Partial<AuthUser>>> {
  const res = await fetch('/api/users/nickname', {
    method: 'POST',
    headers: jsonHeaders,
    credentials: 'include',
    body: JSON.stringify({ nickname }),
  });
  const data = await res.json();
  return data;
}

// 신규: 회원가입 API
export async function signupUser(body: { email: string; password: string; nickname: string; name: string; phone: string; marketing?: boolean; }): Promise<ApiResponse> {
  const res = await fetch('/api/users/signup', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify(body),
  });
  const data = await res.json();
  // 백엔드 응답의 키 차이를 흡수 (is_success 대응)
  if (data && typeof data.is_success === 'boolean' && data.success === undefined) {
    data.success = data.is_success;
  }
  return data;
}
