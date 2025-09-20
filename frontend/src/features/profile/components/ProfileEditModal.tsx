import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useAuth } from '../../user/hooks/useAuth';
import { updateProfile, validateNickname, validateName, transformProfileFormData, formatPhoneNumber } from '../services/ProfileUpdateService';
import type { ProfileFormData } from '../types/ProfileUpdateTypes';

interface ProfileEditModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

const ProfileEditModal: React.FC<ProfileEditModalProps> = ({ open, onClose, onSuccess }) => {
  const { user, refreshMe } = useAuth();
  
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
  } = useForm<ProfileFormData>({
    defaultValues: {
      nickname: '',
      name: '',
      phone: '',
    },
  });

  // 모달이 열릴 때 사용자 정보로 폼 초기화
  useEffect(() => {
    if (open && user) {
      console.log('ProfileEditModal: 사용자 정보로 폼 초기화', { 
        nickname: user.nickname, 
        name: user.name, 
        phone: user.phone 
      });
      
      // setValue를 사용하여 각 필드를 개별적으로 설정
      setValue('nickname', user.nickname || '');
      setValue('name', user.name || '');
      setValue('phone', user.phone || '');
    }
  }, [open, user, setValue]);

  // 모달이 닫힐 때는 별도 리셋 불필요 (다음에 열릴 때 자동으로 사용자 정보로 초기화됨)

  const onSubmit = async (data: ProfileFormData) => {
    try {
      const updateData = transformProfileFormData(data);
      
      // 변경사항이 있는지 확인
      const hasChanges = 
        (updateData.nickname && updateData.nickname !== user?.nickname) ||
        (updateData.name && updateData.name !== user?.name);
      
      if (!hasChanges) {
        alert('변경된 정보가 없습니다.');
        return;
      }

      const response = await updateProfile(updateData);
      
      if (response.success) {
        // 사용자 정보 새로고침
        await refreshMe();
        
        alert('프로필이 성공적으로 업데이트되었습니다.');
        onSuccess?.();
        onClose();
      }
    } catch (error) {
      console.error('Profile update error:', error);
      alert(error instanceof Error ? error.message : '프로필 업데이트 중 오류가 발생했습니다.');
    }
  };

  const handleCancel = () => {
    onClose();
  };

  if (!open) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[80vh] flex flex-col">
        {/* Header */}
        <div className="p-6 border-b border-gray-200">
          <h3 className="text-xl font-bold text-gray-900">프로필 편집</h3>
          <p className="text-sm text-gray-600 mt-1">기본 정보를 수정할 수 있습니다.</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="flex-1 overflow-y-auto">
          <div className="p-6 space-y-4">
            {/* 닉네임 */}
            <div>
              <label htmlFor="nickname" className="block text-sm font-medium text-gray-700 mb-2">
                닉네임 <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="nickname"
                {...register('nickname', {
                  required: '닉네임을 입력해주세요.',
                  validate: (value) => {
                    const validation = validateNickname(value);
                    return validation.isValid || validation.message;
                  },
                })}
                className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${
                  errors.nickname ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
                }`}
                placeholder="2-50자 사이로 입력해주세요"
              />
              {errors.nickname && (
                <p className="text-red-500 text-xs mt-1">{errors.nickname.message}</p>
              )}
            </div>

            {/* 이름 */}
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
                이름 <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="name"
                {...register('name', {
                  required: '이름을 입력해주세요.',
                  validate: (value) => {
                    const validation = validateName(value);
                    return validation.isValid || validation.message;
                  },
                })}
                className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${
                  errors.name ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
                }`}
                placeholder="2-50자 사이로 입력해주세요"
              />
              {errors.name && (
                <p className="text-red-500 text-xs mt-1">{errors.name.message}</p>
              )}
            </div>

            {/* 전화번호 (읽기 전용 - 민감한 정보는 별도 처리) */}
            <div>
              <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-2">
                전화번호
              </label>
              <input
                type="tel"
                id="phone"
                value={user?.phone ? formatPhoneNumber(user.phone) : ''}
                readOnly
                className="w-full px-4 py-3 border border-gray-200 rounded-lg bg-gray-50 text-sm text-gray-500"
                placeholder="전화번호 정보 없음"
              />
              <p className="text-xs text-gray-500 mt-1">
                전화번호 변경은 보안을 위해 별도 절차가 필요합니다.
              </p>
            </div>

            {/* 이메일 (읽기 전용) */}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                이메일
              </label>
              <input
                type="email"
                id="email"
                value={user?.email || ''}
                readOnly
                className="w-full px-4 py-3 border border-gray-200 rounded-lg bg-gray-50 text-sm text-gray-500"
                placeholder="이메일 정보 없음"
              />
              <p className="text-xs text-gray-500 mt-1">
                이메일 변경은 보안을 위해 별도 절차가 필요합니다.
              </p>
            </div>
          </div>

          {/* Footer */}
          <div className="p-6 border-t border-gray-200 flex gap-3">
            <button
              type="button"
              onClick={handleCancel}
              className="flex-1 py-3 px-4 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors"
              disabled={isSubmitting}
            >
              취소
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 py-3 px-4 bg-blue-500 text-white font-medium rounded-lg hover:bg-blue-600 disabled:bg-blue-300 disabled:cursor-not-allowed transition-colors"
            >
              {isSubmitting ? '저장 중...' : '저장'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProfileEditModal;