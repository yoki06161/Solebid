import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useAuth } from '../../user/hooks/useAuth';
import { updateSensitiveProfile, validatePhone, formatPhoneNumber } from '../services/ProfileUpdateService';
import type { SensitiveProfileFormData } from '../types/ProfileUpdateTypes';
import SecurityConfirmDialog from './SecurityConfirmDialog';
import EmailChangeModal from './EmailChangeModal';

interface SensitiveProfileEditModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

const SensitiveProfileEditModal: React.FC<SensitiveProfileEditModalProps> = ({ 
  open, 
  onClose, 
  onSuccess 
}) => {
  const { user, refreshMe } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [pendingFormData, setPendingFormData] = useState<SensitiveProfileFormData | null>(null);
  const [showEmailChangeModal, setShowEmailChangeModal] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
    setValue,
  } = useForm<SensitiveProfileFormData>({
    defaultValues: {
      currentPassword: '',
      phone: '',
    },
  });

  // 모달이 열릴 때 현재 사용자 정보로 폼 초기화
  useEffect(() => {
    if (open && user) {
      // 데이터베이스에서 하이픈 없이 저장된 전화번호를 포맷팅해서 표시
      const formattedPhone = user.phone ? formatPhoneNumber(user.phone) : '';
      setValue('phone', formattedPhone);
      setValue('currentPassword', '');
    }
  }, [open, user, setValue]);

  // 모달이 닫힐 때 폼 리셋
  useEffect(() => {
    if (!open) {
      reset();
      setShowPassword(false);
      setShowConfirmDialog(false);
      setPendingFormData(null);
      setShowEmailChangeModal(false);
    }
  }, [open, reset]);

  const onSubmit = async (data: SensitiveProfileFormData) => {
    // 변경사항이 있는지 확인 (전화번호만) - 숫자만 비교
    const currentPhoneNumbers = user?.phone ? user.phone.replace(/[^\d]/g, '') : '';
    const newPhoneNumbers = data.phone ? data.phone.replace(/[^\d]/g, '') : '';
    const hasChanges = (newPhoneNumbers && newPhoneNumbers !== currentPhoneNumbers);
    
    if (!hasChanges) {
      alert('변경된 정보가 없습니다.');
      return;
    }

    // 확인 다이얼로그 표시
    setPendingFormData(data);
    setShowConfirmDialog(true);
  };

  const handleConfirmUpdate = async () => {
    if (!pendingFormData) return;

    try {
      // API 요청 데이터 준비 (전화번호만)
      const currentPhoneNumbers = user?.phone ? user.phone.replace(/[^\d]/g, '') : '';
      const newPhoneNumbers = pendingFormData.phone ? pendingFormData.phone.replace(/[^\d]/g, '') : '';
      
      const updateData = {
        currentPassword: pendingFormData.currentPassword,
        ...(newPhoneNumbers && newPhoneNumbers !== currentPhoneNumbers && { 
          phone: newPhoneNumbers // 하이픈 제거하고 숫자만 전송
        }),
      };

      console.log('전송할 전화번호 (원본):', pendingFormData.phone);
      console.log('전송할 전화번호 (변환):', pendingFormData.phone?.replace(/[^\d]/g, ''));
      console.log('API 요청 데이터:', updateData);

      const response = await updateSensitiveProfile(updateData);
      
      if (response.success) {
        // 사용자 정보 새로고침
        await refreshMe();
        
        alert('민감한 정보가 성공적으로 업데이트되었습니다.');
        onSuccess?.();
        onClose();
      }
    } catch (error) {
      console.error('Sensitive profile update error:', error);
      alert(error instanceof Error ? error.message : '민감한 정보 업데이트 중 오류가 발생했습니다.');
    } finally {
      setShowConfirmDialog(false);
      setPendingFormData(null);
    }
  };

  const handleCancel = () => {
    reset();
    onClose();
  };

  const togglePasswordVisibility = () => {
    setShowPassword(prev => !prev);
  };

  if (!open) return null;

  return (
    <>
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[80vh] flex flex-col">
          {/* Header */}
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
                <svg className="w-5 h-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 0h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <div>
                <h3 className="text-xl font-bold text-gray-900">민감한 정보 편집</h3>
                <p className="text-sm text-gray-600 mt-1">보안을 위해 현재 비밀번호 확인이 필요합니다.</p>
              </div>
            </div>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit(onSubmit)} className="flex-1 overflow-y-auto">
            <div className="p-6 space-y-6">
              {/* 보안 안내 */}
              <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                <div className="flex items-start space-x-3">
                  <svg className="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.5 0L4.268 18.5c-.77.833.192 2.5 1.732 2.5z" />
                  </svg>
                  <div>
                    <h4 className="text-sm font-medium text-red-800">보안 인증 필요</h4>
                    <p className="text-sm text-red-700 mt-1">
                      전화번호는 민감한 정보로, 변경 시 현재 비밀번호 확인이 필요합니다.
                    </p>
                  </div>
                </div>
              </div>

              {/* 이메일 변경 섹션 */}
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-sm font-medium text-blue-800">이메일 변경</h4>
                    <p className="text-sm text-blue-700 mt-1">
                      현재 이메일: {user?.email || '설정되지 않음'}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => setShowEmailChangeModal(true)}
                    className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
                  >
                    이메일 변경
                  </button>
                </div>
                <p className="text-xs text-blue-600 mt-2">
                  이메일 변경은 별도의 인증 과정을 거쳐 안전하게 처리됩니다.
                </p>
              </div>

              {/* 현재 비밀번호 */}
              <div>
                <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700 mb-2">
                  현재 비밀번호 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    id="currentPassword"
                    {...register('currentPassword', {
                      required: '현재 비밀번호를 입력해주세요.',
                    })}
                    className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${
                      errors.currentPassword ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
                    }`}
                    placeholder="보안 확인을 위해 현재 비밀번호를 입력해주세요"
                  />
                  <button
                    type="button"
                    onClick={togglePasswordVisibility}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showPassword ? (
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                      </svg>
                    ) : (
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    )}
                  </button>
                </div>
                {errors.currentPassword && (
                  <p className="text-red-500 text-xs mt-1">{errors.currentPassword.message}</p>
                )}
              </div>



              {/* 전화번호 */}
              <div>
                <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-2">
                  전화번호
                </label>
                <div className="relative">
                  <input
                    type="tel"
                    id="phone"
                    {...register('phone', {
                      validate: (value) => {
                        if (!value) return true; // 선택사항이므로 빈 값 허용
                        const validation = validatePhone(value);
                        return validation.isValid || validation.message;
                      },
                      onChange: (e) => {
                        // 자동 포맷팅 적용
                        const formatted = formatPhoneNumber(e.target.value);
                        setValue('phone', formatted);
                      },
                    })}
                    className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${
                      errors.phone ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
                    }`}
                    placeholder="전화번호를 입력하세요 (자동으로 하이픈 추가됩니다)"
                    maxLength={13} // 최대 길이 (하이픈 포함)
                  />
                  {/* 전화번호 아이콘 */}
                  <div className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                    </svg>
                  </div>
                </div>
                {errors.phone && (
                  <p className="text-red-500 text-xs mt-1">{errors.phone.message}</p>
                )}
                <div className="flex items-center justify-between mt-1">
                  <p className="text-xs text-gray-500">
                    현재 전화번호: {user?.phone ? formatPhoneNumber(user.phone) : '설정되지 않음'}
                  </p>
                </div>
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
                className="flex-1 py-3 px-4 bg-yellow-600 text-white font-medium rounded-lg hover:bg-yellow-700 disabled:bg-yellow-300 disabled:cursor-not-allowed transition-colors"
              >
                {isSubmitting ? '업데이트 중...' : '업데이트'}
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* 확인 다이얼로그 */}
      <SecurityConfirmDialog
        open={showConfirmDialog}
        title="민감한 정보 변경 확인"
        message="민감한 개인정보를 변경하시겠습니까? 이 작업은 보안상 중요하므로 신중히 결정해주세요."
        type="warning"
        confirmText="변경하기"
        cancelText="취소"
        onConfirm={handleConfirmUpdate}
        onCancel={() => {
          setShowConfirmDialog(false);
          setPendingFormData(null);
        }}
        isLoading={isSubmitting}
      />

      {/* 이메일 변경 모달 */}
      <EmailChangeModal
        open={showEmailChangeModal}
        onClose={() => setShowEmailChangeModal(false)}
        onSuccess={async () => {
          setShowEmailChangeModal(false);
          // 사용자 정보 새로고침 (이메일 변경 반영)
          await refreshMe();
          onSuccess?.();
        }}
      />
    </>
  );
};

export default SensitiveProfileEditModal;