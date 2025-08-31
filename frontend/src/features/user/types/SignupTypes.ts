export interface SignupFormData {
  email: string;
  password: string;
  confirmPassword: string;
  nickname: string;
  name: string;
  phone: string;
}

export interface Agreements {
  all: boolean;
  terms: boolean;
  privacy: boolean;
  marketing: boolean;
}

export interface SignupErrors {
  email: string;
  password: string;
  confirmPassword: string;
  nickname: string;
  name: string;
  phone: string;
  agreeTerms: string;
  agreePrivacy: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
  name: string;
  phone: string;
  marketing?: boolean;
}
