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


