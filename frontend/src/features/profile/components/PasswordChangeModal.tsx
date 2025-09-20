import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useAuth } from '../../user/hooks/useAuth';
import { changePassword, validatePasswordStrength } from '../services/ProfileUpdateService';
import type { PasswordChangeFormData } from '../types/ProfileUpdateTypes';
import PasswordStrengthIndicator from './PasswordStrengthIndicator';
import SecurityConfirmDialog from './SecurityConfirmDialog';

interface PasswordChangeModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

const PasswordChangeModal: React.FC<PasswordChangeModalProps> = ({ 
  open, 
  onClose, 
  onSuccess 
}) => {
  const { logout } = useAuth();
  const [showPassword, setShowPassword] = useState({
    current: false,
    new: false,
    confirm: false,
  });
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [pendingFormData, setPendingFormData] = useState<PasswordChangeFormData | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
    watch,
    trigger,
  } = useForm<PasswordChangeFormData>({
    defaultValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const newPassword = watch('newPassword');
  const confirmPassword = watch('confirmPassword');

  // 모달이 닫힐 때 폼 리셋
  useEffect(() => {
    if (!open) {
      reset();
      setShowPassword({ current: false, new: false, confirm: false });
      setShowConfirmDialog(false);
      setPendingFormData(null);
    }
  }, [open, reset]);

  const togglePasswordVisibility = (field: keyof typeof showPassword) => {
    setShowPassword(prev => ({
      ...prev,
      [field]: !prev[field],
    }));
  };

  const onSubmit = async (data: PasswordChangeFormData) => {
    // 비밀번호 확인 검증
    if (data.newPassword !== data.confirmPassword) {
      return;
    }

    // 현재 비밀번호와 새 비밀번호 동일성 검증
    if (data.currentPassword === data.newPassword) {
      return;
    }

    // 확인 다이얼로그 표시
    setPendingFormData(data);
    setShowConfirmDialog(true);
  };

  const handleConfirmPasswordChange = async () => {
    if (!pendingFormData) return;

    try {
      const response = await changePassword(pendingFormData);
      
      if (response.success) {
        // 사용자에게 로그아웃 안내
        alert('비밀번호가 성공적으로 변경되었습니다.\n보안을 위해 자동으로 로그아웃되며 로그인 페이지로 이동합니다.');
        onSuccess?.();
        onClose();
        
        // 세션이 무효화되었으므로 로그아웃 처리 및 로그인 페이지로 리다이렉트
        if (response.data.sessionInvalidated) {
          // 잠시 대기 후 로그아웃 처리 (사용자가 메시지를 읽을 시간 제공)
          setTimeout(async () => {
            try {
              // 로그아웃 처리 (토큰 삭제 및 사용자 상태 초기화)
              await logout();
              // 로그인 페이지로 리다이렉트
              window.location.href = '/login';
            } catch (error) {
              console.error('Logout error:', error);
              // 로그아웃 실패 시에도 로그인 페이지로 이동
              window.location.href = '/login';
            }
          }, 1500); // 1.5초 후 로그아웃
        }
      }
    } catch (error) {
      console.error('Password change error:', error);
      alert(error instanceof Error ? error.message : '비밀번호 변경 중 오류가 발생했습니다.');
    } finally {
      setShowConfirmDialog(false);
      setPendingFormData(null);
    }
  };

  const handleCancel = () => {
    reset();
    onClose();
  };

  if (!open) return null;

  return (
    <>
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] flex flex-col">
          {/* Header */}
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center">
                <svg className="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 0h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <div>
                <h3 className="text-xl font-bold text-gray-900">비밀번호 변경</h3>
                <p className="text-sm text-gray-600 mt-1">보안을 위해 강력한 비밀번호를 설정해주세요.</p>
              </div>
            </div>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit(onSubmit)} className="flex-1 overflow-y-auto">
            <div className="p-6 space-y-6">
              {/* 현재 비밀번호 */}
              <div>
                <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700 mb-2">
                  현재 비밀번호 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type={showPassword.current ? 'text' : 'password'}
                    id="currentPassword"
                    {...register('currentPassword', {
                      required: '현재 비밀번호를 입력해주세요.',
                    })}
                    className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${
                      errors.currentPassword ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
                    }`}
                    placeholder="현재 비밀번호를 입력해주세요"
                  />
                  <button
                    type="button"
                    onClick={() => togglePasswordVisibility('current')}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showPassword.current ? (
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

              {/* 새 비밀번호 */}
              <div>
                <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 mb-2">
                  새 비밀번호 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type={showPassword.new ? 'text' : 'password'}
                    id="newPassword"
                    {...register('newPassword', {
                      required: '새 비밀번호를 입력해주세요.',
                      validate: (value) => {
                        const validation = validatePasswordStrength(value);
                        return validation.isValid || validation.message;
                      },
                      onChange: () => {
                        if (confirmPassword) {
                          trigger('confirmPassword');
                        }
                      },
                    })}
                    className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${
                      errors.newPassword ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
                    }`}
                    placeholder="새 비밀번호를 입력해주세요"
                  />
                  <button
                    type="button"
                    onClick={() => togglePasswordVisibility('new')}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showPassword.new ? (
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
                {errors.newPassword && (
                  <p className="text-red-500 text-xs mt-1">{errors.newPassword.message}</p>
                )}
                
                {/* 비밀번호 강도 표시기 */}
                <PasswordStrengthIndicator password={newPassword || ''} className="mt-3" />
              </div>

              {/* 비밀번호 확인 */}
              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
                  비밀번호 확인 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type={showPassword.confirm ? 'text' : 'password'}
                    id="confirmPassword"
                    {...register('confirmPassword', {
                      required: '비밀번호 확인을 입력해주세요.',
                      validate: (value) => {
                        if (value !== newPassword) {
                          return '새 비밀번호와 일치하지 않습니다.';
                        }
                        if (value === watch('currentPassword')) {
                          return '새 비밀번호는 현재 비밀번호와 달라야 합니다.';
                        }
                        return true;
                      },
                    })}
                    className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${
                      errors.confirmPassword ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
                    }`}
                    placeholder="비밀번호를 다시 입력해주세요"
                  />
                  <button
                    type="button"
                    onClick={() => togglePasswordVisibility('confirm')}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showPassword.confirm ? (
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
                {errors.confirmPassword && (
                  <p className="text-red-500 text-xs mt-1">{errors.confirmPassword.message}</p>
                )}
              </div>

              {/* 보안 안내 */}
              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                <div className="flex items-start space-x-3">
                  <svg className="w-5 h-5 text-yellow-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.5 0L4.268 18.5c-.77.833.192 2.5 1.732 2.5z" />
                  </svg>
                  <div>
                    <h4 className="text-sm font-medium text-yellow-800">보안 안내</h4>
                    <p className="text-sm text-yellow-700 mt-1">
                      비밀번호 변경 시 보안을 위해 모든 기기에서 자동으로 로그아웃됩니다.
                    </p>
                  </div>
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
                className="flex-1 py-3 px-4 bg-red-600 text-white font-medium rounded-lg hover:bg-red-700 disabled:bg-red-300 disabled:cursor-not-allowed transition-colors"
              >
                {isSubmitting ? '변경 중...' : '비밀번호 변경'}
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* 확인 다이얼로그 */}
      <SecurityConfirmDialog
        open={showConfirmDialog}
        title="비밀번호 변경 확인"
        message="정말로 비밀번호를 변경하시겠습니까? 변경 후 모든 기기에서 자동으로 로그아웃됩니다."
        type="warning"
        confirmText="변경하기"
        cancelText="취소"
        onConfirm={handleConfirmPasswordChange}
        onCancel={() => {
          setShowConfirmDialog(false);
          setPendingFormData(null);
        }}
        isLoading={isSubmitting}
      />
    </>
  );
};

export default PasswordChangeModal;