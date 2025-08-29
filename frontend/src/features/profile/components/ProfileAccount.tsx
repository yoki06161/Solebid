import { Link, useNavigate } from "react-router-dom";
import { useCallback, useEffect, useState } from "react";

type AuthUser = {
    [key: string]: unknown;
    socialProvider?: string;
    provider?: string;
    nickname?: string;
    email?: string;
    userId?: number;
};

const ProfileAccount = () => {
    const navigate = useNavigate();
    const [provider, setProvider] = useState<string | null>(null);

    const dispatchAuthChanged = useCallback((user: AuthUser | null) => {
        try {
            const evt: CustomEvent<{ user: AuthUser | null }> = new CustomEvent('auth-changed', { detail: { user } });
            window.dispatchEvent(evt);
        } catch (e) {
            console.debug('dispatchAuthChanged 이벤트 전파 실패', e);
        }
    }, []);

    // 세션에서 소셜 제공자 파악 (OAuth2Callback/헤더의 /me 반영 모두 지원)
    useEffect(() => {
        const syncFromSession = () => {
            try {
                const raw = sessionStorage.getItem('auth.user');
                if (!raw) { setProvider(null); return; }
                const obj: AuthUser = JSON.parse(raw);
                const pv = (obj?.socialProvider as string | undefined) || (obj?.provider as string | undefined) || null; // e.g., "Kakao" | "Google"
                setProvider(pv);
            } catch (e) {
                console.debug('세션에서 소셜 제공자 동기화 실패', e);
                setProvider(null);
            }
        };
        syncFromSession();
        const handler: EventListener = () => { syncFromSession(); };
        window.addEventListener('auth-changed', handler);
        return () => window.removeEventListener('auth-changed', handler);
    }, []);

    const handleLogout = useCallback(async () => {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
            });
        } catch (e) {
            console.error('logout error', e);
        } finally {
            try { sessionStorage.removeItem('auth.user'); } catch (e) { console.debug('세션 제거 실패', e); }
            dispatchAuthChanged(null);
            navigate('/');
        }
    }, [dispatchAuthChanged, navigate]);

    const handleWithdraw = useCallback(async () => {
        const ok = window.confirm('정말로 회원 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
        if (!ok) return;
        try {
            const res = await fetch('/api/users/me', {
                method: 'DELETE',
                credentials: 'include',
            });
            const data = await res.json().catch(() => ({}));
            if (res.ok && (data?.success ?? false)) {
                alert('회원탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.');
                try { sessionStorage.removeItem('auth.user'); } catch (e) { console.debug('세션 제거 실패', e); }
                dispatchAuthChanged(null);
                navigate('/');
            } else {
                alert(data?.message || '회원탈퇴 처리에 실패했습니다. 잠시 후 다시 시도해주세요.');
            }
        } catch (e) {
            console.error('withdraw error', e);
            alert('네트워크 오류가 발생했습니다. 다시 시도해주세요.');
        }
    }, [dispatchAuthChanged, navigate]);

    const handleUnlink = useCallback(async (prov: 'kakao' | 'google') => {
        const ok = window.confirm(`${prov === 'kakao' ? '카카오' : '구글'} 계정 연결을 해제할까요?`);
        if (!ok) return;
        try {
            const res = await fetch(`/api/users/me/social/${prov}`, {
                method: 'DELETE',
                credentials: 'include',
            });
            const data = await res.json().catch(() => ({}));
            if (res.ok && data?.success) {
                // 세션의 provider 정보를 제거하고 반영
                try {
                    const raw = sessionStorage.getItem('auth.user');
                    const obj: AuthUser | null = raw ? JSON.parse(raw) : null;
                    if (obj) {
                        const cleaned: AuthUser = { ...obj };
                        delete cleaned.provider;
                        delete cleaned.socialProvider;
                        sessionStorage.setItem('auth.user', JSON.stringify(cleaned));
                        dispatchAuthChanged(cleaned);
                    }
                } catch (e) { console.debug('세션 provider 정리 실패', e); }
                const hint: string | undefined = data?.data?.manualRevokeHint;
                alert((data?.message || '연결이 해제되었습니다.') + (hint ? `\n\n${hint}` : ''));
                setProvider(null);
            } else {
                alert(data?.message || '연결 해제에 실패했습니다. 잠시 후 다시 시도해주세요.');
            }
        } catch (e) {
            console.error('unlink error', e);
            alert('네트워크 오류가 발생했습니다. 다시 시도해주세요.');
        }
    }, [dispatchAuthChanged]);

    const renderUnlinkSection = () => {
        if (!provider) return null;
        const pvLower = String(provider).toLowerCase();
        const isKakao = pvLower === 'kakao';
        const isGoogle = pvLower === 'google';
        return (
            <div className="mt-4 p-3 bg-gray-50 border border-gray-200 rounded-lg">
                <div className="flex items-center justify-between">
                    <div className="text-sm text-gray-700">
                        연결된 소셜 계정: <span className="font-medium">{provider}</span>
                    </div>
                    {isKakao && (
                        <button
                            onClick={() => handleUnlink('kakao')}
                            className="px-3 py-1.5 bg-yellow-400 text-gray-900 rounded-md hover:bg-yellow-300 cursor-pointer !rounded-button whitespace-nowrap">
                            카카오 연결 해제
                        </button>
                    )}
                    {isGoogle && (
                        <button
                            onClick={() => handleUnlink('google')}
                            className="px-3 py-1.5 bg-white border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 cursor-pointer !rounded-button whitespace-nowrap">
                            구글 연결 해제
                        </button>
                    )}
                </div>
            </div>
        );
    };

    return (
        <div className="bg-white rounded-lg shadow-sm p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
                계정 관리
            </h3>
            {renderUnlinkSection()}
            <div className="space-y-3 mt-4">
                <button
                    onClick={handleLogout}
                    className="w-full px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 cursor-pointer !rounded-button whitespace-nowrap">
                    <i className="fas fa-sign-out-alt mr-2" />
                    로그아웃
                </button>
            </div>
            <div className="mt-6 pt-4 border-t border-gray-200">
                <div className="text-center space-y-2">
                    <button
                        onClick={handleWithdraw}
                        className="block w-full text-gray-600 text-sm hover:text-gray-900 cursor-pointer">
                        회원 탈퇴
                    </button>
                    <Link
                        to="#"
                        className="block text-gray-600 text-sm hover:text-gray-900 cursor-pointer">
                        개인정보 처리방침
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default ProfileAccount;
