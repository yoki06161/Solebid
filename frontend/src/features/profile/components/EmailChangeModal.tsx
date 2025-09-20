import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useAuth } from '../../user/hooks/useAuth';
import { requestEmailChange, confirmEmailChange, validateEmail } from '../services/ProfileUpdateService';

interface EmailChangeModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

interface Step1FormData {
  currentPassword: string;
  newEmail: string;
}

interface Step2FormData {
  verificationCode: string;
}

const EmailChangeModal: React.FC<EmailChangeModalProps> = ({
  open,
  onClose,
  onSuccess
}) => {
  const { user, refreshMe } = useAuth();
  const [step, setStep] = useState<1 | 2>(1);
  const [showPassword, setShowPassword] = useState(false);
  const [pendingEmail, setPendingEmail] = useState<string>('');
  const [countdown, setCountdown] = useState<number>(0);

  // Step 1 폼 (비밀번호 + 새 이메일)
  const step1Form = useForm<Step1FormData>({
    defaultValues: {
      currentPassword: '',
      newEmail: '',
    },
  });

  // Step 2 폼 (인증 코드)
  const step2Form = useForm<Step2FormData>({
    defaultValues: {
      verificationCode: '',
    },
  });

  // 모달이 열릴 때 초기화
  useEffect(() => {
    if (open) {
      setStep(1);
      setPendingEmail('');
      setCountdown(0);
      step1Form.reset();
      step2Form.reset();
      setShowPassword(false);
    }
  }, [open, step1Form, step2Form]);

  // 카운트다운 타이머
  useEffect(() => {
    let timer: number;
    if (countdown > 0) {
      timer = setTimeout(() => setCountdown(countdown - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [countdown]);

  // Step 1: 인증 코드 발송 요청
  const onStep1Submit = async (data: Step1FormData) => {
    try {
      await requestEmailChange(data.currentPassword, data.newEmail);
      setPendingEmail(data.newEmail);
      setStep(2);
      setCountdown(300); // 5분 카운트다운
    } catch (error) {
      alert(error instanceof Error ? error.message : '이메일 변경 요청 중 오류가 발생했습니다.');
    }
  };

  // Step 2: 인증 코드 검증 및 이메일 변경
  const onStep2Submit = async (data: Step2FormData) => {
    try {
      const response = await confirmEmailChange(pendingEmail, data.verificationCode);

      if (response.success) {
        // 사용자 정보 새로고침 (새로운 토큰으로 자동 갱신됨)
        await refreshMe();

        alert('이메일이 성공적으로 변경되었습니다.\n보안을 위해 새로운 인증 토큰이 발급되었습니다.');
        onSuccess?.();
        onClose();
      }
    } catch (error) {
      alert(error instanceof Error ? error.message : '이메일 변경 중 오류가 발생했습니다.');
    }
  };

  // 인증 코드 재발송
  const handleResendCode = async () => {
    if (countdown > 0) return;

    try {
      const currentPassword = step1Form.getValues('currentPassword');
      await requestEmailChange(currentPassword, pendingEmail);
      setCountdown(300); // 5분 카운트다운 재시작
      alert('인증 코드가 재발송되었습니다.');
    } catch (error) {
      alert(error instanceof Error ? error.message : '인증 코드 재발송 중 오류가 발생했습니다.');
    }
  };

  const handleCancel = () => {
    step1Form.reset();
    step2Form.reset();
    onClose();
  };

  const togglePasswordVisibility = () => {
    setShowPassword(prev => !prev);
  };

  const formatTime = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  if (!open) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[80vh] flex flex-col">
        {/* Header */}
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
              <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
              </svg>
            </div>
            <div>
              <h3 className="text-xl font-bold text-gray-900">이메일 변경</h3>
              <p className="text-sm text-gray-600 mt-1">
                {step === 1 ? '보안을 위해 현재 비밀번호 확인이 필요합니다.' : '새로운 이메일로 발송된 인증 코드를 입력해주세요.'}
              </p>
            </div>
          </div>
        </div>

        {/* Step Indicator */}
        <div className="px-6 py-4 bg-gray-50">
          <div className="flex items-center space-x-4">
            <div className={`flex items-center space-x-2 ${step >= 1 ? 'text-blue-600' : 'text-gray-400'}`}>
              <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium ${step >= 1 ? 'bg-blue-600 text-white' : 'bg-gray-300 text-gray-600'
                }`}>
                1
              </div>
              <span className="text-sm font-medium">비밀번호 확인</span>
            </div>
            <div className="flex-1 h-px bg-gray-300"></div>
            <div className={`flex items-center space-x-2 ${step >= 2 ? 'text-blue-600' : 'text-gray-400'}`}>
              <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-medium ${step >= 2 ? 'bg-blue-600 text-white' : 'bg-gray-300 text-gray-600'
                }`}>
                2
              </div>
              <span className="text-sm font-medium">이메일 인증</span>
            </div>
          </div>
        </div>

        {/* Form Content */}
        <div className="flex-1 overflow-y-auto">
          {step === 1 ? (
            <form onSubmit={step1Form.handleSubmit(onStep1Submit)} className="p-6 space-y-6">
              {/* 보안 안내 */}
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div className="flex items-start space-x-3">
                  <svg className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <div>
                    <h4 className="text-sm font-medium text-blue-800">이메일 변경 절차</h4>
                    <p className="text-sm text-blue-700 mt-1">
                      보안을 위해 현재 비밀번호를 확인한 후, 새로운 이메일로 인증 코드를 발송합니다.
                    </p>
                  </div>
                </div>
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
                    {...step1Form.register('currentPassword', {
                      required: '현재 비밀번호를 입력해주세요.',
                    })}
                    className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${step1Form.formState.errors.currentPassword ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
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
                {step1Form.formState.errors.currentPassword && (
                  <p className="text-red-500 text-xs mt-1">{step1Form.formState.errors.currentPassword.message}</p>
                )}
              </div>

              {/* 새로운 이메일 */}
              <div>
                <label htmlFor="newEmail" className="block text-sm font-medium text-gray-700 mb-2">
                  새로운 이메일 <span className="text-red-500">*</span>
                </label>
                <input
                  type="email"
                  id="newEmail"
                  {...step1Form.register('newEmail', {
                    required: '새로운 이메일을 입력해주세요.',
                    validate: (value) => {
                      const validation = validateEmail(value);
                      return validation.isValid || validation.message;
                    },
                  })}
                  className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${step1Form.formState.errors.newEmail ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
                    }`}
                  placeholder="새로운 이메일 주소를 입력해주세요"
                />
                {step1Form.formState.errors.newEmail && (
                  <p className="text-red-500 text-xs mt-1">{step1Form.formState.errors.newEmail.message}</p>
                )}
                <p className="text-xs text-gray-500 mt-1">
                  현재 이메일: {user?.email || '설정되지 않음'}
                </p>
              </div>

              {/* Footer */}
              <div className="flex gap-3 pt-4">
                <button
                  type="button"
                  onClick={handleCancel}
                  className="flex-1 py-3 px-4 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors"
                  disabled={step1Form.formState.isSubmitting}
                >
                  취소
                </button>
                <button
                  type="submit"
                  disabled={step1Form.formState.isSubmitting}
                  className="flex-1 py-3 px-4 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:bg-blue-300 disabled:cursor-not-allowed transition-colors"
                >
                  {step1Form.formState.isSubmitting ? '발송 중...' : '인증 코드 발송'}
                </button>
              </div>
            </form>
          ) : (
            <form onSubmit={step2Form.handleSubmit(onStep2Submit)} className="p-6 space-y-6">
              {/* 인증 안내 */}
              <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                <div className="flex items-start space-x-3">
                  <svg className="w-5 h-5 text-green-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <div>
                    <h4 className="text-sm font-medium text-green-800">인증 코드 발송 완료</h4>
                    <p className="text-sm text-green-700 mt-1">
                      <span className="font-medium">{pendingEmail}</span>로 6자리 인증 코드를 발송했습니다.
                    </p>
                  </div>
                </div>
              </div>

              {/* 인증 코드 입력 */}
              <div>
                <label htmlFor="verificationCode" className="block text-sm font-medium text-gray-700 mb-2">
                  인증 코드 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  id="verificationCode"
                  maxLength={6}
                  {...step2Form.register('verificationCode', {
                    required: '인증 코드를 입력해주세요.',
                    pattern: {
                      value: /^\d{6}$/,
                      message: '6자리 숫자를 입력해주세요.',
                    },
                  })}
                  className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm text-center font-mono text-lg tracking-widest ${step2Form.formState.errors.verificationCode ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
                    }`}
                  placeholder="123456"
                />
                {step2Form.formState.errors.verificationCode && (
                  <p className="text-red-500 text-xs mt-1">{step2Form.formState.errors.verificationCode.message}</p>
                )}

                {/* 카운트다운 및 재발송 */}
                <div className="flex items-center justify-between mt-2">
                  <p className="text-xs text-gray-500">
                    {countdown > 0 ? (
                      <span className="text-orange-600 font-medium">
                        남은 시간: {formatTime(countdown)}
                      </span>
                    ) : (
                      <span className="text-red-600">인증 코드가 만료되었습니다.</span>
                    )}
                  </p>
                  <button
                    type="button"
                    onClick={handleResendCode}
                    disabled={countdown > 0}
                    className="text-xs text-blue-600 hover:text-blue-800 disabled:text-gray-400 disabled:cursor-not-allowed"
                  >
                    {countdown > 0 ? '재발송 대기 중' : '인증 코드 재발송'}
                  </button>
                </div>
              </div>

              {/* Footer */}
              <div className="flex gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => setStep(1)}
                  className="flex-1 py-3 px-4 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors"
                  disabled={step2Form.formState.isSubmitting}
                >
                  이전 단계
                </button>
                <button
                  type="submit"
                  disabled={step2Form.formState.isSubmitting}
                  className="flex-1 py-3 px-4 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:bg-blue-300 disabled:cursor-not-allowed transition-colors"
                >
                  {step2Form.formState.isSubmitting ? '변경 중...' : '이메일 변경'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default EmailChangeModal;