import type { ApiResponse } from '../types/AuthTypes';

const jsonHeaders = { 'Content-Type': 'application/json' } as const;

export async function requestPasswordReset(email: string): Promise<ApiResponse> {
  const res = await fetch('/api/auth/password/forgot', {
    method: 'POST',
    headers: jsonHeaders,
    credentials: 'include',
    body: JSON.stringify({ email }),
  });
  return res.json();
}

export async function submitPasswordReset(token: string, newPassword: string): Promise<ApiResponse> {
  const res = await fetch('/api/auth/password/reset', {
    method: 'POST',
    headers: jsonHeaders,
    credentials: 'include',
    body: JSON.stringify({ token, newPassword }),
  });
  return res.json();
}

