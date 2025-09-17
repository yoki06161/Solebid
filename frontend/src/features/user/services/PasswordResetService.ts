import type { ApiResponse } from '../types/AuthTypes';

const jsonHeaders = { 'Content-Type': 'application/json' } as const;

/**
 * 비밀번호 재설정 OTP 요청
 */
export async function requestPasswordResetOtp(email: string): Promise<ApiResponse> {
  const res = await fetch('/api/auth/password/request-reset', {
    method: 'POST',
    headers: jsonHeaders,
    credentials: 'include',
    body: JSON.stringify({ email }),
  });
  return res.json();
}

/**
 * OTP 검증만 수행 (비밀번호 재설정 없이)
 */
export async function verifyPasswordResetOtp(
  email: string, 
  otp: string
): Promise<ApiResponse> {
  const res = await fetch('/api/auth/password/verify-otp', {
    method: 'POST',
    headers: jsonHeaders,
    credentials: 'include',
    body: JSON.stringify({ email, otp }),
  });
  return res.json();
}

/**
 * OTP 검증 및 비밀번호 재설정
 */
export async function verifyOtpAndResetPassword(
  email: string, 
  otp: string, 
  newPassword: string
): Promise<ApiResponse> {
  const res = await fetch('/api/auth/password/verify-and-reset', {
    method: 'POST',
    headers: jsonHeaders,
    credentials: 'include',
    body: JSON.stringify({ email, otp, newPassword }),
  });
  return res.json();
}

/**
 * 비밀번호 재설정 OTP 재전송
 */
export async function resendPasswordResetOtp(email: string): Promise<ApiResponse> {
  const res = await fetch('/api/auth/password/resend-otp', {
    method: 'POST',
    headers: jsonHeaders,
    credentials: 'include',
    body: JSON.stringify({ email }),
  });
  return res.json();
}



