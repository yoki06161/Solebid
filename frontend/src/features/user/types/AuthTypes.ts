export type Provider = 'google' | 'kakao';

export interface ApiResponse<T = unknown> {
  success: boolean;
  data?: T;
  message?: string;
  errorCode?: string;
}

export interface AuthUser {
  userId?: number;
  email?: string;
  nickname?: string;
  userType?: string;
  provider?: string;
  requiresNickname?: boolean;
  temperature?: number; // 사용자 온도 정보
}

// 완전한 사용자 프로필 정보 (온도 포함)
export interface UserProfile {
  userId: number;
  email: string;
  nickname: string;
  userType: string;
  temperature: number; // 사용자 온도 정보
}

export interface LoginForm {
  email: string;
  password: string;
}

export interface OAuth2AuthUrl {
  authUrl: string;
  state: string;
  provider: string;
}

export interface TokenStatus {
  isAuthenticated: boolean;
  accessTokenExpiresIn: number;
  refreshAvailable: boolean;
}


