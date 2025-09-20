// 프로필 업데이트 관련 TypeScript 타입 정의

export interface ProfileUpdateRequest {
  nickname?: string;
  name?: string;
}

export interface SensitiveProfileUpdateRequest {
  currentPassword: string;
  email?: string;
  phone?: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ProfileUpdateResponse {
  userId: number;
  email: string;
  nickname: string;
  name: string;
  phone: string;
  userType: string;
  temperature: number;
}

export interface PasswordChangeResponse {
  message: string;
  sessionInvalidated: boolean;
}

// 폼 유효성 검사 타입
export interface ProfileFormData {
  nickname: string;
  name: string;
  phone: string;
}

export interface SensitiveProfileFormData {
  currentPassword: string;
  phone: string;
}

export interface PasswordChangeFormData {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// API 응답 래퍼 타입
export interface ProfileUpdateApiResponse {
  success: boolean;
  data: ProfileUpdateResponse;
  message: string;
}

export interface PasswordChangeApiResponse {
  success: boolean;
  data: PasswordChangeResponse;
  message: string;
}