import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { LoginForm, Provider } from '../types/AuthTypes';
import { cacheUserAndEmit, getOAuth2AuthUrl } from '../services/AuthService';
import { loginWithEmail, reactivateAccount } from '../services/UserService';

export function useEmailLogin() {
  const navigate = useNavigate();
  const [loginForm, setLoginForm] = useState<LoginForm>({ email: '', password: '' });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setLoginForm((prev) => ({ ...prev, [name]: value }));
    if (errors[name] || errors.submit) {
      setErrors((prev) => ({ ...prev, [name]: '', submit: '' }));
    }
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    if (!loginForm.email.trim()) newErrors.email = '이메일을 입력해주세요.';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(loginForm.email)) newErrors.email = '올바른 이메일 형식을 입력해주세요.';
    if (!loginForm.password.trim()) newErrors.password = '비밀번호를 입력해주세요.';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleReactivateFlow = async (token: string) => {
    const res = await reactivateAccount(token);
    if (res && res.success) {
      cacheUserAndEmit(res.data);
      navigate('/');
      return true;
    }
    setErrors({ submit: res?.message || '계정 재활성화에 실패했습니다.' });
    return false;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;
    setIsLoading(true);
    try {
      const result = await loginWithEmail(loginForm);
      if (result.success) {
        setErrors({});
        cacheUserAndEmit(result.data);
        navigate('/');
      } else if (result?.errorCode === 'WITHDRAWN_USER') {
        const token = (result.data as { reactivationToken?: unknown } | undefined)?.reactivationToken;
        if (typeof token === 'string' && token) {
          const ok = window.confirm('회원 탈퇴 처리된 계정입니다. 계정을 다시 활성화하시겠습니까?');
          if (ok) await handleReactivateFlow(token);
          else setErrors({ submit: '재활성화가 취소되었습니다.' });
        } else {
          setErrors({ submit: result.message || '재활성화 토큰을 확인할 수 없습니다.' });
        }
      } else {
        setErrors({ submit: result.message || '로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.' });
      }
    } catch (error) {
      console.error('Login error:', error);
      setErrors({ submit: '네트워크 오류가 발생했습니다. 다시 시도해주세요.' });
    } finally {
      setIsLoading(false);
    }
  };

  // 소셜 로그인 시작
  const handleSocialLogin = async (provider: Provider) => {
    try {
      setIsLoading(true);
      const data = await getOAuth2AuthUrl(provider);
      if (data.success && data.data?.authUrl) {
        window.location.href = data.data.authUrl;
      } else {
        setErrors({ submit: data.message || `${provider} 로그인 URL 생성에 실패했습니다.` });
        setIsLoading(false);
      }
    } catch (error) {
      console.error(`${provider} 로그인 오류:`, error);
      setErrors({ submit: `${provider} 로그인 중 오류가 발생했습니다.` });
      setIsLoading(false);
    }
  };

  return { loginForm, errors, isLoading, handleInputChange, handleSubmit, handleSocialLogin };
}
