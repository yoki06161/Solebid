import { apiFetch } from '../../../utils/apiFetch';
import type { ApiResponse, UserProfile } from '../../user/types/AuthTypes';

/**
 * 사용자 프로필 정보를 가져오는 API 함수
 * 온도 정보를 포함한 완전한 사용자 프로필을 반환
 */
export const fetchUserProfile = async (): Promise<UserProfile> => {
  try {
    const response = await apiFetch<ApiResponse<UserProfile>>('/api/users/me');
    
    if (!response.success || !response.data) {
      throw new Error('사용자 프로필 정보를 찾을 수 없습니다.');
    }
    
    // 온도 정보가 없는 경우 기본값 설정
    const profile = response.data;
    if (typeof profile.temperature !== 'number') {
      profile.temperature = 36.5;
    }
    
    return profile;
  } catch (error) {
    // 네트워크 오류나 서버 오류 시 적절한 에러 메시지 제공
    if (error instanceof Error) {
      if (error.message.indexOf('HTTP 401') !== -1) {
        throw new Error('로그인이 필요합니다.');
      } else if (error.message.indexOf('HTTP 403') !== -1) {
        throw new Error('접근 권한이 없습니다.');
      } else if (error.message.indexOf('HTTP 404') !== -1) {
        throw new Error('사용자 정보를 찾을 수 없습니다.');
      } else if (error.message.indexOf('HTTP 500') !== -1) {
        throw new Error('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
      throw error;
    }
    throw new Error('온도 정보를 불러올 수 없습니다.');
  }
};