import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { requestPasswordResetOtp, verifyPasswordResetOtp, resendPasswordResetOtp, verifyOtpAndResetPassword } from '../services/PasswordResetService';
import VerificationCodeInput from '../components/VerificationCodeInput';
import type { ApiResponse } from '../types/AuthTypes';

type Step = 'email' | 'otp' | 'password';

const FindPassword: React.FC = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>('email');
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resendCooldown, setResendCooldown] = useState(0);
  const [otpTimer, setOtpTimer] = useState(0); // OTP 만료 타이머 (초)
  const [otpExpired, setOtpExpired] = useState(false);

  // 정확한 타이머 관리를 위한 시작 시간 저장
  const [timerStartTime, setTimerStartTime] = useState<number | null>(null);
  const [resendStartTime, setResendStartTime] = useState<number | null>(null);

  // 재전송 쿨다운 타이머 - 정확한 시간 기반
  useEffect(() => {
    if (resendCooldown > 0 && resendStartTime) {
      const targetTime = resendStartTime + (60 * 1000); // 60초
      
      const updateTimer = () => {
        const now = Date.now();
        const remaining = Math.max(0, Math.ceil((targetTime - now) / 1000));
        
        if (remaining !== resendCooldown) {
          setResendCooldown(remaining);
        }
        
        if (remaining > 0) {
          requestAnimationFrame(updateTimer);
        }
      };
      
      requestAnimationFrame(updateTimer);
    }
  }, [resendCooldown > 0, resendStartTime]);

  // OTP 만료 타이머 - 정확한 시간 기반
  useEffect(() => {
    if (otpTimer > 0 && timerStartTime) {
      const targetTime = timerStartTime + (300 * 1000); // 5분
      
      const updateTimer = () => {
        const now = Date.now();
        const remaining = Math.max(0, Math.ceil((targetTime - now) / 1000));
        
        if (remaining !== otpTimer) {
          setOtpTimer(remaining);
        }
        
        if (remaining > 0) {
          requestAnimationFrame(updateTimer);
        } else if (remaining === 0 && step === 'otp' && !otpExpired) {
          setOtpExpired(true);
          setError('인증번호가 만료되었습니다. 새로운 인증번호를 요청하세요.');
        }
      };
      
      requestAnimationFrame(updateTimer);
    }
  }, [otpTimer > 0, timerStartTime, step, otpExpired]);

  const handleEmailSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      setError('이메일을 입력하세요');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res: ApiResponse = await requestPasswordResetOtp(email.trim());
      if (res.success) {
        const now = Date.now();
        setStep('otp');
        setResendCooldown(60); // 1분 쿨다운
        setResendStartTime(now);
        setOtpTimer(300); // 5분 OTP 만료 시간 (300초)
        setTimerStartTime(now);
        setOtpExpired(false);
      } else {
        setError(res.message || '요청 실패');
      }
    } catch (e: any) {
      setError(e.message || '요청 중 오류');
    } finally {
      setLoading(false);
    }
  };

  const handleOtpSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp || otp.length !== 6) {
      setError('6자리 인증번호를 입력하세요');
      return;
    }
    if (!/^\d{6}$/.test(otp)) {
      setError('인증번호는 숫자만 입력 가능합니다');
      return;
    }
    
    setLoading(true);
    setError(null);
    try {
      const res: ApiResponse = await verifyPasswordResetOtp(email, otp);
      if (res.success) {
        setStep('password');
      } else {
        setError(res.message || '인증번호가 올바르지 않습니다');
      }
    } catch (e: any) {
      setError(e.message || '인증 중 오류가 발생했습니다');
    } finally {
      setLoading(false);
    }
  };

  const validatePassword = (password: string): string | null => {
    if (!password || password.length < 8) {
      return '비밀번호는 8자 이상이어야 합니다.';
    }
    if (password.length > 64) {
      return '비밀번호는 64자 이하여야 합니다.';
    }
    return null;
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const passwordError = validatePassword(newPassword);
    if (passwordError) {
      setError(passwordError);
      return;
    }
    
    if (newPassword !== confirmPassword) {
      setError('비밀번호가 일치하지 않습니다');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const res: ApiResponse = await verifyOtpAndResetPassword(email, otp, newPassword);
      if (res.success) {
        navigate('/password-reset-success');
      } else {
        setError(res.message || '비밀번호 변경에 실패했습니다');
      }
    } catch (e: any) {
      setError(e.message || '비밀번호 변경 중 오류가 발생했습니다');
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    if (resendLoading || resendCooldown > 0) return;

    setResendLoading(true);
    setError(null);

    try {
      const res: ApiResponse = await resendPasswordResetOtp(email);
      if (res.success) {
        const now = Date.now();
        setResendCooldown(60); // 1분 쿨다운
        setResendStartTime(now);
        setOtp(''); // OTP 입력 필드 초기화
        setOtpTimer(300); // 5분 OTP 만료 시간 재설정
        setTimerStartTime(now);
        setOtpExpired(false); // 만료 상태 초기화
      } else {
        setError(res.message || '인증번호 재전송에 실패했습니다');
      }
    } catch (e: any) {
      setError(e.message || '재전송 중 오류가 발생했습니다');
    } finally {
      setResendLoading(false);
    }
  };

  const handleOtpChange = (value: string) => {
    setOtp(value);
    if (error && error.includes('인증번호')) {
      setError(null);
    }
  };

  // 시간을 MM:SS 형식으로 포맷
  const formatTime = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  // 남은 시간에 따른 스타일 결정
  const getTimerStyle = (): string => {
    if (otpExpired || otpTimer <= 0) {
      return 'text-red-600 font-semibold';
    } else if (otpTimer < 60) {
      // 1분 미만: 빨간색
      return 'text-red-500 font-semibold';
    } else {
      // 1분 이상: 회색
      return 'text-gray-600';
    }
  };

  const handlePasswordChange = (value: string, isConfirm: boolean = false) => {
    if (isConfirm) {
      setConfirmPassword(value);
    } else {
      setNewPassword(value);
    }
    
    if (error && (error.includes('비밀번호') || error.includes('일치'))) {
      setError(null);
    }
  };

  const goBack = () => {
    if (step === 'otp') {
      setStep('email');
      setOtp('');
      setError(null);
      setOtpExpired(false);
      setOtpTimer(0);
    } else if (step === 'password') {
      setStep('otp');
      setNewPassword('');
      setConfirmPassword('');
      setError(null);
    }
  };

  const renderEmailStep = () => (
    <form onSubmit={handleEmailSubmit} className="space-y-5">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">이메일</label>
        <input
          type="email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          placeholder="example@domain.com"
          disabled={loading}
        />
      </div>
      {error && <div className="text-xs text-red-500">{error}</div>}
      <button
        type="submit"
        disabled={loading}
        className="w-full py-3 bg-blue-600 text-white font-medium !rounded-button hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center text-sm"
      >
        {loading ? '전송 중...' : '인증번호 받기'}
      </button>
    </form>
  );

  const renderOtpStep = () => (
    <form onSubmit={handleOtpSubmit} className="space-y-6">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-3">인증번호</label>
        <VerificationCodeInput
          value={otp}
          onChange={handleOtpChange}
          disabled={loading || otpExpired}
          error={error !== null && error.includes('인증번호')}
        />
        
        {/* OTP 타이머 표시 */}
        <div className="mt-3 text-center">
          <div className={`text-sm mb-2 ${getTimerStyle()}`}>
            {otpExpired || otpTimer <= 0 
              ? '인증번호가 만료되었습니다' 
              : `남은 시간: ${formatTime(otpTimer)}`
            }
          </div>
        </div>
        
        <div className="mt-3 text-center">
          <button
            type="button"
            onClick={handleResendOtp}
            disabled={resendLoading || resendCooldown > 0}
            className="text-sm text-blue-600 hover:text-blue-800 disabled:text-gray-400 disabled:cursor-not-allowed"
          >
            {resendLoading 
              ? '재전송 중...' 
              : resendCooldown > 0 
                ? `재전송 (${resendCooldown}초 후)` 
                : '인증번호 재전송'
            }
          </button>
        </div>
      </div>
      {error && <div className="text-xs text-red-500 text-center">{error}</div>}
      <button
        type="submit"
        disabled={loading || otpExpired}
        className="w-full py-3 bg-blue-600 text-white font-medium !rounded-button hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center text-sm"
      >
        {loading ? '확인 중...' : '인증번호 확인'}
      </button>
      <button
        type="button"
        onClick={goBack}
        className="w-full text-sm text-gray-500 hover:text-gray-700"
        disabled={loading}
      >
        ← 이메일 입력으로 돌아가기
      </button>
    </form>
  );

  const renderPasswordStep = () => (
    <form onSubmit={handlePasswordSubmit} className="space-y-5">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">새 비밀번호</label>
        <input
          type="password"
          value={newPassword}
          onChange={(e) => handlePasswordChange(e.target.value)}
          className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          placeholder="8자 이상 입력하세요"
          disabled={loading}
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">새 비밀번호 확인</label>
        <input
          type="password"
          value={confirmPassword}
          onChange={(e) => handlePasswordChange(e.target.value, true)}
          className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          placeholder="비밀번호를 다시 입력하세요"
          disabled={loading}
        />
      </div>
      {error && <div className="text-xs text-red-500">{error}</div>}
      <button
        type="submit"
        disabled={loading}
        className="w-full py-3 bg-blue-600 text-white font-medium !rounded-button hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center text-sm"
      >
        {loading ? '변경 중...' : '비밀번호 변경'}
      </button>
      <button
        type="button"
        onClick={goBack}
        className="w-full text-sm text-gray-500 hover:text-gray-700"
        disabled={loading}
      >
        ← 인증번호 입력으로 돌아가기
      </button>
    </form>
  );

  const getStepTitle = () => {
    switch (step) {
      case 'email':
        return '비밀번호 찾기';
      case 'otp':
        return '인증번호 입력';
      case 'password':
        return '새 비밀번호 설정';
      default:
        return '비밀번호 찾기';
    }
  };

  const getStepDescription = () => {
    switch (step) {
      case 'email':
        return '가입한 이메일로 6자리 인증번호를 보내드립니다.';
      case 'otp':
        return `${email}로 발송된 6자리 인증번호를 입력하세요.`;
      case 'password':
        return '새로운 비밀번호를 설정하세요.';
      default:
        return '';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4">
      <div className="bg-white shadow-lg rounded-lg w-full max-w-md p-8">
        <h1 className="text-2xl font-bold mb-2 text-gray-800">{getStepTitle()}</h1>
        <p className="text-sm text-gray-500 mb-6">{getStepDescription()}</p>
        
        {step === 'email' && renderEmailStep()}
        {step === 'otp' && renderOtpStep()}
        {step === 'password' && renderPasswordStep()}
      </div>
    </div>
  );
};

export default FindPassword;

