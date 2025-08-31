import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { ApiResponse, AuthUser } from '../types/AuthTypes';
import { cacheUserAndEmit, postOAuth2Callback } from '../services/AuthService';
import { reactivateAccount } from '../services/UserService';

export type CallbackStatus = 'loading' | 'success' | 'error';

export function useOAuth2Callback() {
  const navigate = useNavigate();
  const { provider } = useParams<{ provider: string }>();
  const processedRef = useRef(false);
  const [status, setStatus] = useState<CallbackStatus>('loading');
  const [message, setMessage] = useState('로그인 처리 중...');

  useEffect(() => {
    (async () => {
      if (processedRef.current) return;
      processedRef.current = true;

      try {
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');
        const state = urlParams.get('state');
        if (!code || !state || !provider) {
          setStatus('error');
          setMessage('잘못된 요청입니다.');
          return;
        }

        const guardKey = `oauth2_callback_handled_${state}`;
        if (sessionStorage.getItem(guardKey)) {
          setStatus('success');
          setMessage('이미 로그인 처리가 완료되었습니다. 메인 페이지로 이동합니다.');
          setTimeout(() => { navigate('/'); window.location.replace('/'); }, 1000);
          return;
        }

        const data: ApiResponse<AuthUser> = await postOAuth2Callback(provider, code, state);
        if (data.success) {
          sessionStorage.setItem(guardKey, '1');
          cacheUserAndEmit(data.data);
          setStatus('success');
          setMessage('로그인이 완료되었습니다. 잠시 후 이동합니다.');
          const requiresNickname = Boolean(data.data?.requiresNickname);
          setTimeout(() => { navigate(requiresNickname ? '/nickname-setup' : '/'); }, 600);
        } else if (data?.errorCode === 'WITHDRAWN_USER') {
          const token = (data.data as { reactivationToken?: unknown } | undefined)?.reactivationToken;
          if (typeof token === 'string' && token) {
            const ok = window.confirm('회원 탈퇴 처리된 계정입니다. 계정을 다시 활성화하시겠습니까?');
            if (ok) {
              const r = await reactivateAccount(token);
              if (r.success) {
                cacheUserAndEmit(r.data);
                setStatus('success');
                setMessage('계정이 재활성화되었습니다. 잠시 후 이동합니다.');
                setTimeout(() => navigate('/'), 600);
              } else {
                setStatus('error');
                setMessage(r?.message || '계정 재활성화에 실패했습니다.');
              }
            } else {
              setStatus('error');
              setMessage('재활성화가 취소되었습니다.');
            }
          } else {
            setStatus('error');
            setMessage(data.message || '재활성화 토큰을 확인할 수 없습니다.');
          }
        } else {
          setStatus('error');
          setMessage(data.message || '로그인에 실패했습니다.');
        }
      } catch (e) {
        console.error('OAuth2 콜백 처리 오류:', e);
        setStatus('error');
        setMessage('로그인 처리 중 오류가 발생했습니다.');
      }
    })();
  }, [provider, navigate]);

  const retry = () => navigate('/login');

  return { status, message, provider, retry };
}
