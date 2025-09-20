import { apiFetch } from '../../../utils/apiFetch';
import type {
  ProfileUpdateRequest,
  SensitiveProfileUpdateRequest,
  PasswordChangeRequest,
  ProfileUpdateApiResponse,
  PasswordChangeApiResponse,
} from '../types/ProfileUpdateTypes';

/**
 * 일반 프로필 정보 업데이트 API 함수
 * 닉네임, 이름 등 민감하지 않은 정보를 업데이트
 */
export const updateProfile = async (data: ProfileUpdateRequest): Promise<ProfileUpdateApiResponse> => {
  try {
    const response = await apiFetch<ProfileUpdateApiResponse>('/api/users/profile', {
      method: 'PUT',
      json: data,
    });

    if (!response.success) {
      throw new Error(response.message || '프로필 업데이트에 실패했습니다.');
    }

    return response;
  } catch (error) {
    if (error instanceof Error) {
      if (error.message.includes('HTTP 401')) {
        throw new Error('로그인이 필요합니다.');
      } else if (error.message.includes('HTTP 403')) {
        throw new Error('접근 권한이 없습니다.');
      } else if (error.message.includes('HTTP 409')) {
        throw new Error('이미 사용 중인 닉네임입니다.');
      } else if (error.message.includes('HTTP 400')) {
        throw new Error('입력한 정보가 올바르지 않습니다.');
      } else if (error.message.includes('HTTP 500')) {
        throw new Error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
      throw error;
    }
    throw new Error('프로필 업데이트 중 오류가 발생했습니다.');
  }
};

/**
 * 민감한 프로필 정보 업데이트 API 함수 (스텝업 인증)
 * 이메일, 전화번호 등 민감한 정보를 업데이트
 * 현재 비밀번호 확인이 필요
 */
export const updateSensitiveProfile = async (data: SensitiveProfileUpdateRequest): Promise<ProfileUpdateApiResponse> => {
  try {
    const response = await apiFetch<ProfileUpdateApiResponse>('/api/users/profile/sensitive', {
      method: 'PUT',
      json: data,
    });

    if (!response.success) {
      throw new Error(response.message || '민감한 정보 업데이트에 실패했습니다.');
    }

    return response;
  } catch (error) {
    if (error instanceof Error) {
      if (error.message.includes('HTTP 401')) {
        throw new Error('현재 비밀번호가 올바르지 않습니다.');
      } else if (error.message.includes('HTTP 403')) {
        throw new Error('접근 권한이 없습니다.');
      } else if (error.message.includes('HTTP 409')) {
        throw new Error('이미 사용 중인 이메일입니다.');
      } else if (error.message.includes('HTTP 400')) {
        throw new Error('입력한 정보가 올바르지 않습니다.');
      } else if (error.message.includes('HTTP 500')) {
        throw new Error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
      throw error;
    }
    throw new Error('민감한 정보 업데이트 중 오류가 발생했습니다.');
  }
};

/**
 * 비밀번호 변경 API 함수
 * 현재 비밀번호 확인 후 새 비밀번호로 변경
 * 성공 시 모든 세션이 무효화됨
 */
export const changePassword = async (data: PasswordChangeRequest): Promise<PasswordChangeApiResponse> => {
  try {
    // 비밀번호 확인 검증
    if (data.newPassword !== data.confirmPassword) {
      throw new Error('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
    }

    // 현재 비밀번호와 새 비밀번호 동일성 검증
    if (data.currentPassword === data.newPassword) {
      throw new Error('새 비밀번호는 현재 비밀번호와 달라야 합니다.');
    }

    const response = await apiFetch<PasswordChangeApiResponse>('/api/users/password', {
      method: 'PUT',
      json: {
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
        confirmPassword: data.confirmPassword,
      },
    });

    if (!response.success) {
      throw new Error(response.message || '비밀번호 변경에 실패했습니다.');
    }

    return response;
  } catch (error) {
    if (error instanceof Error) {
      if (error.message.includes('HTTP 401')) {
        throw new Error('현재 비밀번호가 올바르지 않습니다.');
      } else if (error.message.includes('HTTP 400')) {
        throw new Error('비밀번호 형식이 올바르지 않습니다. 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.');
      } else if (error.message.includes('HTTP 403')) {
        throw new Error('접근 권한이 없습니다.');
      } else if (error.message.includes('HTTP 500')) {
        throw new Error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
      throw error;
    }
    throw new Error('비밀번호 변경 중 오류가 발생했습니다.');
  }
};

/**
 * 비밀번호 강도 검증 함수
 * 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 함
 */
export const validatePasswordStrength = (password: string): { isValid: boolean; message: string } => {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/;

  if (!password) {
    return { isValid: false, message: '비밀번호를 입력해주세요.' };
  }

  if (password.length < 8) {
    return { isValid: false, message: '비밀번호는 최소 8자 이상이어야 합니다.' };
  }

  if (password.length > 20) {
    return { isValid: false, message: '비밀번호는 최대 20자까지 가능합니다.' };
  }

  if (!passwordRegex.test(password)) {
    return { isValid: false, message: '영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.' };
  }

  return { isValid: true, message: '사용 가능한 비밀번호입니다.' };
};

/**
 * 닉네임 유효성 검증 함수
 */
export const validateNickname = (nickname: string): { isValid: boolean; message: string } => {
  if (!nickname) {
    return { isValid: false, message: '닉네임을 입력해주세요.' };
  }

  if (nickname.length < 2) {
    return { isValid: false, message: '닉네임은 최소 2자 이상이어야 합니다.' };
  }

  if (nickname.length > 50) {
    return { isValid: false, message: '닉네임은 최대 50자까지 가능합니다.' };
  }

  return { isValid: true, message: '사용 가능한 닉네임입니다.' };
};

/**
 * 이름 유효성 검증 함수
 */
export const validateName = (name: string): { isValid: boolean; message: string } => {
  if (!name) {
    return { isValid: false, message: '이름을 입력해주세요.' };
  }

  if (name.length < 2) {
    return { isValid: false, message: '이름은 최소 2자 이상이어야 합니다.' };
  }

  if (name.length > 50) {
    return { isValid: false, message: '이름은 최대 50자까지 가능합니다.' };
  }

  return { isValid: true, message: '사용 가능한 이름입니다.' };
};

/**
 * 전화번호 자동 포맷팅 함수
 * 다양한 한국 전화번호 형식을 지원 (휴대폰, 지역번호, 특수번호)
 */
export const formatPhoneNumber = (value: string): string => {
  // 숫자만 추출
  const numbers = value.replace(/[^\d]/g, '');

  // 최대 11자리로 제한
  const truncated = numbers.slice(0, 11);

  if (truncated.length <= 2) {
    return truncated;
  }

  // 02 (서울 지역번호) - 2자리 지역번호
  if (truncated.startsWith('02')) {
    if (truncated.length <= 2) {
      return truncated;
    } else if (truncated.length <= 5) {
      return `${truncated.slice(0, 2)}-${truncated.slice(2)}`;
    } else if (truncated.length <= 9) {
      return `${truncated.slice(0, 2)}-${truncated.slice(2, 5)}-${truncated.slice(5)}`;
    } else {
      return `${truncated.slice(0, 2)}-${truncated.slice(2, 6)}-${truncated.slice(6)}`;
    }
  }

  // 010 (휴대폰) - 3자리 시작
  if (truncated.startsWith('010')) {
    if (truncated.length <= 3) {
      return truncated;
    } else if (truncated.length <= 7) {
      return `${truncated.slice(0, 3)}-${truncated.slice(3)}`;
    } else {
      return `${truncated.slice(0, 3)}-${truncated.slice(3, 7)}-${truncated.slice(7)}`;
    }
  }

  // 1588, 1577, 1544 등 특수번호 - 4자리 시작
  if (truncated.match(/^(15\d\d|16\d\d|18\d\d)/)) {
    if (truncated.length <= 4) {
      return truncated;
    } else {
      return `${truncated.slice(0, 4)}-${truncated.slice(4)}`;
    }
  }

  // 기타 지역번호 (031, 032, 033, 041, 042, 043, 051, 052, 053, 054, 055, 061, 062, 063, 064) - 3자리 시작
  if (truncated.match(/^(0[3-6]\d)/)) {
    if (truncated.length <= 3) {
      return truncated;
    } else if (truncated.length <= 6) {
      return `${truncated.slice(0, 3)}-${truncated.slice(3)}`;
    } else if (truncated.length <= 10) {
      return `${truncated.slice(0, 3)}-${truncated.slice(3, 6)}-${truncated.slice(6)}`;
    } else {
      return `${truncated.slice(0, 3)}-${truncated.slice(3, 7)}-${truncated.slice(7)}`;
    }
  }

  // 기본 처리 (3-3-4 또는 3-4-4 형식)
  if (truncated.length <= 3) {
    return truncated;
  } else if (truncated.length <= 6) {
    return `${truncated.slice(0, 3)}-${truncated.slice(3)}`;
  } else if (truncated.length <= 10) {
    return `${truncated.slice(0, 3)}-${truncated.slice(3, 6)}-${truncated.slice(6)}`;
  } else {
    return `${truncated.slice(0, 3)}-${truncated.slice(3, 7)}-${truncated.slice(7)}`;
  }
};

/**
 * 전화번호에서 숫자만 추출하는 함수
 */
export const extractPhoneNumbers = (phone: string): string => {
  return phone.replace(/[^\d]/g, '');
};

/**
 * 전화번호 유효성 검증 함수
 * 다양한 한국 전화번호 형식을 지원
 */
export const validatePhone = (phone: string): { isValid: boolean; message: string } => {
  if (!phone) {
    return { isValid: false, message: '전화번호를 입력해주세요.' };
  }

  // 숫자만 추출해서 검증
  const numbers = extractPhoneNumbers(phone);

  // 최소 길이 확인
  if (numbers.length < 8) {
    return { isValid: false, message: '전화번호가 너무 짧습니다.' };
  }

  // 최대 길이 확인
  if (numbers.length > 11) {
    return { isValid: false, message: '전화번호가 너무 깁니다.' };
  }

  // 휴대폰 번호 (010-xxxx-xxxx)
  if (numbers.startsWith('010')) {
    if (numbers.length !== 11) {
      return { isValid: false, message: '휴대폰 번호는 11자리여야 합니다. (예: 010-1234-5678)' };
    }
    return { isValid: true, message: '사용 가능한 휴대폰 번호입니다.' };
  }

  // 서울 지역번호 (02-xxx-xxxx 또는 02-xxxx-xxxx)
  if (numbers.startsWith('02')) {
    if (numbers.length < 9 || numbers.length > 10) {
      return { isValid: false, message: '서울 지역번호는 9-10자리여야 합니다. (예: 02-1234-5678)' };
    }
    return { isValid: true, message: '사용 가능한 서울 지역번호입니다.' };
  }

  // 기타 지역번호 (031, 032, 033, 041, 042, 043, 051, 052, 053, 054, 055, 061, 062, 063, 064)
  if (numbers.match(/^(0[3-6]\d)/)) {
    if (numbers.length < 9 || numbers.length > 11) {
      return { isValid: false, message: '지역번호는 9-11자리여야 합니다. (예: 031-123-4567)' };
    }
    return { isValid: true, message: '사용 가능한 지역번호입니다.' };
  }

  // 특수번호 (1588, 1577, 1544 등)
  if (numbers.match(/^(15\d\d|16\d\d|18\d\d)/)) {
    if (numbers.length < 8 || numbers.length > 9) {
      return { isValid: false, message: '특수번호는 8-9자리여야 합니다. (예: 1588-1234)' };
    }
    return { isValid: true, message: '사용 가능한 특수번호입니다.' };
  }

  return { isValid: false, message: '올바른 전화번호 형식이 아닙니다.' };
};

/**
 * 이메일 유효성 검증 함수
 */
export const validateEmail = (email: string): { isValid: boolean; message: string } => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!email) {
    return { isValid: false, message: '이메일을 입력해주세요.' };
  }

  if (!emailRegex.test(email)) {
    return { isValid: false, message: '올바른 이메일 형식이 아닙니다.' };
  }

  return { isValid: true, message: '사용 가능한 이메일입니다.' };
};
/**

 * 폼 데이터를 API 요청 형식으로 변환하는 유틸리티 함수
 */
export const transformProfileFormData = (formData: any): ProfileUpdateRequest => {
  const result: ProfileUpdateRequest = {};

  if (formData.nickname && formData.nickname.trim()) {
    result.nickname = formData.nickname.trim();
  }

  if (formData.name && formData.name.trim()) {
    result.name = formData.name.trim();
  }

  return result;
};

/**
 * 민감한 정보 폼 데이터를 API 요청 형식으로 변환하는 유틸리티 함수
 */
export const transformSensitiveProfileFormData = (formData: any): SensitiveProfileUpdateRequest => {
  const result: SensitiveProfileUpdateRequest = {
    currentPassword: formData.currentPassword,
  };

  if (formData.email && formData.email.trim()) {
    result.email = formData.email.trim();
  }

  if (formData.phone && formData.phone.trim()) {
    result.phone = formData.phone.trim();
  }

  return result;
};

/**
 * 이메일 변경 요청 API 함수 (1단계: 인증 코드 발송)
 * 현재 비밀번호 확인 후 새로운 이메일로 인증 코드 발송
 */
export const requestEmailChange = async (currentPassword: string, newEmail: string): Promise<{ success: boolean; message: string }> => {
  try {
    const response = await apiFetch<{ success: boolean; message: string; data: any }>('/api/users/email/change-request', {
      method: 'POST',
      json: {
        currentPassword,
        newEmail,
      },
    });

    if (!response.success) {
      throw new Error(response.message || '이메일 변경 요청에 실패했습니다.');
    }

    return response;
  } catch (error) {
    if (error instanceof Error) {
      if (error.message.includes('HTTP 401')) {
        throw new Error('현재 비밀번호가 올바르지 않습니다.');
      } else if (error.message.includes('HTTP 409')) {
        throw new Error('이미 사용 중인 이메일입니다.');
      } else if (error.message.includes('HTTP 400')) {
        if (error.message.includes('EMAIL_SAME_AS_CURRENT')) {
          throw new Error('새로운 이메일이 현재 이메일과 동일합니다.');
        }
        throw new Error('입력한 정보가 올바르지 않습니다.');
      } else if (error.message.includes('HTTP 500')) {
        throw new Error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
      throw error;
    }
    throw new Error('이메일 변경 요청 중 오류가 발생했습니다.');
  }
};

/**
 * 이메일 변경 완료 API 함수 (2단계: 인증 코드 검증 및 이메일 업데이트)
 * 인증 코드를 검증하고 이메일을 변경
 */
export const confirmEmailChange = async (newEmail: string, verificationCode: string): Promise<ProfileUpdateApiResponse> => {
  try {
    const response = await apiFetch<ProfileUpdateApiResponse>('/api/users/email/change-confirm', {
      method: 'POST',
      json: {
        newEmail,
        verificationCode,
      },
    });

    if (!response.success) {
      throw new Error(response.message || '이메일 변경에 실패했습니다.');
    }

    return response;
  } catch (error) {
    if (error instanceof Error) {
      if (error.message.includes('HTTP 400')) {
        if (error.message.includes('EMAIL_VERIFICATION_TOKEN_INVALID')) {
          throw new Error('인증 코드가 올바르지 않습니다.');
        } else if (error.message.includes('EMAIL_VERIFICATION_TOKEN_EXPIRED')) {
          throw new Error('인증 코드가 만료되었습니다. 다시 요청해주세요.');
        }
        throw new Error('입력한 정보가 올바르지 않습니다.');
      } else if (error.message.includes('HTTP 409')) {
        throw new Error('이미 사용 중인 이메일입니다.');
      } else if (error.message.includes('HTTP 401')) {
        throw new Error('로그인이 필요합니다.');
      } else if (error.message.includes('HTTP 500')) {
        throw new Error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
      throw error;
    }
    throw new Error('이메일 변경 중 오류가 발생했습니다.');
  }
};