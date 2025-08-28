import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

const OAuth2Callback: React.FC = () => {
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('로그인 처리 중...');
  const navigate = useNavigate();
  const { provider } = useParams<{ provider: string }>();
  const processedRef = useRef(false);

  const reactivate = async (token: string) => {
    try {
      const res = await fetch('/api/users/reactivate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ token }),
      });
      const data = await res.json();
      if (res.ok && data.success) {
        try {
          sessionStorage.setItem('auth.user', JSON.stringify(data.data));
          const evt = new CustomEvent('auth-changed', { detail: { user: data.data } });
          window.dispatchEvent(evt);
        } catch (e) { console.debug('OAuth2Callback: 재활성 캐시 실패', e); }
        setStatus('success');
        setMessage('계정이 재활성화되었습니다. 잠시 후 이동합니다.');
        setTimeout(() => navigate('/'), 600);
        return true;
      }
      setStatus('error');
      setMessage(data?.message || '계정 재활성화에 실패했습니다.');
      return false;
    } catch (e) {
      console.error('OAuth2Callback: reactivate error', e);
      setStatus('error');
      setMessage('네트워크 오류가 발생했습니다. 다시 시도해주세요.');
      return false;
    }
  };

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
          setTimeout(() => {
            navigate('/');
            window.location.replace('/');
          }, 1000);
          return;
        }

        const response = await fetch(`/api/auth/oauth2/${provider}/callback`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({ code, state })
        });

        const data = await response.json();

        if (data.success) {
          sessionStorage.setItem(guardKey, '1');
          try {
            if (data.data) {
              sessionStorage.setItem('auth.user', JSON.stringify(data.data));
              const evt = new CustomEvent('auth-changed', { detail: { user: data.data } });
              window.dispatchEvent(evt);
            }
          } catch (e) {
            console.debug('OAuth2Callback: 세션 캐시/이벤트 반영 실패', e);
          }

          setStatus('success');
          setMessage('로그인이 완료되었습니다. 잠시 후 이동합니다.');

          const requiresNickname: boolean = Boolean(data.data?.requiresNickname);
          setTimeout(() => {
            if (requiresNickname) {
              navigate('/nickname-setup');
            } else {
              navigate('/');
            }
          }, 600);
        } else if (data?.errorCode === 'WITHDRAWN_USER' && data?.data?.reactivationToken) {
          const ok = window.confirm('회원 탈퇴 처리된 계정입니다. 계정을 다시 활성화하시겠습니까?');
          if (ok) {
            await reactivate(String(data.data.reactivationToken));
          } else {
            setStatus('error');
            setMessage('재활성화가 취소되었습니다.');
          }
        } else {
          setStatus('error');
          setMessage(data.message || '로그인에 실패했습니다.');
        }
      } catch (error) {
        console.error('OAuth2 콜백 처리 오류:', error);
        setStatus('error');
        setMessage('로그인 처리 중 오류가 발생했습니다.');
      }
    })();
  }, [provider, navigate]);

  const handleRetry = () => {
    navigate('/login');
  };

  const getProviderName = (provider: string) => {
    switch (provider) {
      case 'google':
        return '구글';
      case 'kakao':
        return '카카오';
      default:
        return provider;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-6">
      <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md text-center">
        {status === 'loading' && (
          <div className="space-y-4">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto">
              <i className="fas fa-spinner fa-spin text-2xl text-blue-600"></i>
            </div>
            <h3 className="text-xl font-semibold text-gray-900">
              {getProviderName(provider || '')} 로그인 처리 중
            </h3>
            <p className="text-gray-600">{message}</p>
          </div>
        )}
        
        {status === 'success' && (
          <div className="space-y-4">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
              <i className="fas fa-check text-2xl text-green-600"></i>
            </div>
            <h3 className="text-xl font-semibold text-gray-900">로그인 성공!</h3>
            <p className="text-gray-600">{message}</p>
          </div>
        )}
        
        {status === 'error' && (
          <div className="space-y-4">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto">
              <i className="fas fa-times text-2xl text-red-600"></i>
            </div>
            <h3 className="text-xl font-semibold text-gray-900">로그인 실패</h3>
            <p className="text-gray-600">{message}</p>
            <button 
              onClick={handleRetry}
              className="w-full py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 cursor-pointer transition-colors"
            >
              <i className="fas fa-redo mr-2"></i>
              다시 시도
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default OAuth2Callback;