import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cacheUserAndEmit } from '../services/AuthService';
import { checkNicknameAvailability, updateNickname } from '../services/UserService';

export function useNicknameSetup() {
  const navigate = useNavigate();
  const [nickname, setNickname] = useState('');
  const [available, setAvailable] = useState<boolean | null>(null);
  const [checking, setChecking] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isBasicValid = useMemo(() => {
    return nickname.trim().length >= 2 && nickname.trim().length <= 10 && !nickname.startsWith('user_');
  }, [nickname]);

  const checkAvailability = async (value: string) => {
    if (!value || value.trim().length < 2) {
      setAvailable(null);
      return;
    }
    try {
      setChecking(true);
      const res = await checkNicknameAvailability(value.trim());
      if (res.success) setAvailable(Boolean(res.data?.available));
      else setAvailable(false);
    } catch (e) {
      console.debug('닉네임 중복 확인 실패', e);
      setAvailable(false);
    } finally {
      setChecking(false);
    }
  };

  useEffect(() => {
    const handle = setTimeout(() => {
      if (isBasicValid) checkAvailability(nickname);
    }, 300);
    return () => clearTimeout(handle);
  }, [nickname, isBasicValid]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!isBasicValid) { setError('닉네임은 2~10자이며 user_로 시작할 수 없습니다.'); return; }
    if (available === false) { setError('이미 사용 중인 닉네임입니다.'); return; }
    try {
      setSubmitting(true);
      const res = await updateNickname(nickname.trim());
      if (res.success) {
        try {
          const cachedRaw = sessionStorage.getItem('auth.user');
          const cached = cachedRaw ? JSON.parse(cachedRaw) as Record<string, unknown> : {};
          const merged = { ...cached, ...(res.data as Record<string, unknown> | undefined), nickname: (res.data as { nickname?: unknown } | undefined)?.nickname ?? nickname.trim() };
          cacheUserAndEmit(merged as any);
        } catch (e) {
          console.debug('닉네임 설정 후 세션 갱신 실패', e);
        }
        navigate('/');
      } else {
        setError(res.message || '닉네임 설정에 실패했습니다.');
      }
    } catch (e) {
      console.debug('닉네임 설정 요청 실패', e);
      setError('네트워크 오류가 발생했습니다. 다시 시도해주세요.');
    } finally {
      setSubmitting(false);
    }
  };

  return { nickname, setNickname, available, checking, submitting, error, setError, isBasicValid, submit };
}
