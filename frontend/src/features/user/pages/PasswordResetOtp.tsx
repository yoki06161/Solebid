import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { verifyOtpAndResetPassword, resendPasswordResetOtp } from '../services/PasswordResetService';
import VerificationCodeInput from '../components/VerificationCodeInput';
import type { ApiResponse } from '../types/AuthTypes';

const PasswordResetOtp: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const email = searchParams.get('email') || '';

  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resendCooldown, setResendCooldown] = useState(0);
  const [otpTimer, setOtpTimer] = useState(300); // OTP 만료 타이머 (초) - 5분으로 초기화
  const [isOtpExpired, setIsOtpExpired] = useState(false);
  
  // 정확한 타이머 관리를 위한 시작 시간 저장
  const [timerStartTime, setTimerStartTime] = useState<number>(Date.now());
  const [resendStartTime, setResendStartTime] = useState<number | null>(null);

  // 에러 메시지 안전 추출 헬퍼
  const extractErrorMessage = (err: unknown, fallback: string): string => {
    if (err instanceof Error) return err.message;
    if (typeof err === 'string') return err;
    if (typeof err === 'object' && err !== null && 'message' in err) {
      const msg = (err as Record<string, unknown>).message;
      if (typeof msg === 'string') return msg;
    }
    return fallback;
  };

  // 이메일이 없으면 비밀번호 찾기 페이지로 리다이렉트
  useEffect(() => {
    if (!email) {
      navigate('/find-password');
    }
  }, [email, navigate]);

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
        } else if (remaining === 0 && !isOtpExpired) {
          setIsOtpExpired(true);
          setError('인증번호가 만료되었습니다. 새로운 인증번호를 요청하세요.');
        }
      };
      
      requestAnimationFrame(updateTimer);
    }
  }, [otpTimer > 0, timerStartTime, isOtpExpired]);

  const validatePassword = (password: string): string | null => {
    if (!password || password.length < 8) {
      return '비밀번호는 8자 이상이어야 합니다.';
    }
    if (password.length > 64) {
      return '비밀번호는 64자 이하여야 합니다.';
    }
    return null;
  };

  const validateForm = (): string | null => {
    if (isOtpExpired) {
      return '인증번호가 만료되었습니다. 재전송을 요청하세요.';
    }

    if (!otp || otp.length !== 6) {
      return '6자리 인증번호를 입력하세요.';
    }
    if (!/^\d{6}$/.test(otp)) {
      return '인증번호는 숫자만 입력 가능합니다.';
    }

    const passwordError = validatePassword(newPassword);
    if (passwordError) {
      return passwordError;
    }

    if (newPassword !== confirmPassword) {
      return '비밀번호가 일치하지 않습니다.';
    }

    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (loading) return;

    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const res: ApiResponse = await verifyOtpAndResetPassword(email, otp, newPassword);
      if (res.success) {
        navigate('/password-reset-success');
      } else {
        setError(res.message || '인증번호 검증에 실패했습니다.');
      }
    } catch (err) {
      setError(extractErrorMessage(err, '요청 중 오류가 발생했습니다.'));
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
        setIsOtpExpired(false); // 만료 상태 리셋
      } else {
        setError(res.message || '인증번호 재전송에 실패했습니다.');
      }
    } catch (err) {
      setError(extractErrorMessage(err, '재전송 중 오류가 발생했습니다.'));
    } finally {
      setResendLoading(false);
    }
  };

  const handleOtpChange = (value: string) => {
    setOtp(value);
    // 에러가 있고 OTP가 변경되면 에러 초기화
    if (error && error.includes('인증번호')) {
      setError(null);
    }
  };

  const handlePasswordChange = (value: string, isConfirm: boolean = false) => {
    if (isConfirm) {
      setConfirmPassword(value);
    } else {
      setNewPassword(value);
    }

    // 비밀번호 관련 에러가 있으면 실시간으로 검증
    if (error && (error.includes('비밀번호') || error.includes('일치'))) {
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
    if (isOtpExpired || otpTimer <= 0) {
      return 'text-red-600 font-semibold';
    } else if (otpTimer < 60) {
      // 1분 미만: 빨간색
      return 'text-red-500 font-semibold';
    } else {
      // 1분 이상: 회색
      return 'text-gray-600';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4">
      <div className="bg-white shadow-lg rounded-lg w-full max-w-md p-8">
        <h1 className="text-2xl font-bold mb-2 text-gray-800">비밀번호 재설정</h1>
        <p className="text-sm text-gray-500 mb-6">
          <span className="font-medium">{email}</span>로 발송된 6자리 인증번호를 입력하고<br />
          새로운 비밀번호를 설정하세요.
        </p>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* OTP 입력 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-3">인증번호</label>
            <VerificationCodeInput
              value={otp}
              onChange={handleOtpChange}
              disabled={loading || isOtpExpired}
              error={error !== null && error.includes('인증번호')}
            />

            {/* OTP 타이머 표시 */}
            <div className="mt-2 text-center">
              <div className={`text-sm mb-2 ${getTimerStyle()}`}>
                {isOtpExpired || otpTimer <= 0
                  ? '인증번호가 만료되었습니다'
                  : `남은 시간: ${formatTime(otpTimer)}`
                }
              </div>
            </div>

            <div className="mt-3 text-center">
              <button
                type="button"
                onClick={handleResendOtp}
                disabled={resendLoading || (resendCooldown > 0 && !isOtpExpired)}
                className="text-sm text-blue-600 hover:text-blue-800 disabled:text-gray-400 disabled:cursor-not-allowed"
              >
                {resendLoading
                  ? '재전송 중...'
                  : resendCooldown > 0 && !isOtpExpired
                    ? `재전송 (${resendCooldown}초 후)`
                    : '인증번호 재전송'
                }
              </button>
            </div>
          </div>

          {/* 새 비밀번호 입력 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">새 비밀번호</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => handlePasswordChange(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
              placeholder="8자 이상 입력하세요"
              disabled={loading || isOtpExpired}
            />
          </div>

          {/* 비밀번호 확인 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">새 비밀번호 확인</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => handlePasswordChange(e.target.value, true)}
              className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
              placeholder="비밀번호를 다시 입력하세요"
              disabled={loading || isOtpExpired}
            />
          </div>

          {/* 에러 메시지 */}
          {error && (
            <div className="text-xs text-red-500 text-center">{error}</div>
          )}

          {/* 제출 버튼 */}
          <button
            type="submit"
            disabled={loading || isOtpExpired}
            className="w-full py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center text-sm"
          >
            {loading ? '처리 중...' : isOtpExpired ? '인증번호 만료됨' : '비밀번호 변경'}
          </button>
        </form>

        {/* 뒤로가기 링크 */}
        <div className="mt-6 text-center">
          <button
            type="button"
            onClick={() => navigate('/find-password')}
            className="text-sm text-gray-500 hover:text-gray-700"
            disabled={loading}
          >
            ← 이메일 입력으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
};

export default PasswordResetOtp;