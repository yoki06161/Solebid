import { useEffect, useState, useCallback } from "react";
import { useNavigate, useLocation } from "react-router-dom";

function AppHeader() {
    const navigate = useNavigate();
    const location = useLocation();
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    const applyCachedUser = useCallback(() => {
        try {
            const raw = sessionStorage.getItem('auth.user');
            if (raw) {
                const cached = JSON.parse(raw);
                if (cached && (cached.nickname || cached.email || cached.userId)) {
                    setIsLoggedIn(true);
                    return true;
                }
            }
        } catch (e) {
            console.debug('applyCachedUser: Failed to parse session', e);
        }
        return false;
    }, []);

    const fetchMe = useCallback(async () => {
        const hadCache = applyCachedUser();
        try {
            const res = await fetch('/api/users/me', { credentials: 'include' });
            const data = await res.json();
            if (res.ok && data.success) {
                const user = data.data || null;
                setIsLoggedIn(true);
                try {
                    sessionStorage.setItem('auth.user', JSON.stringify(user));
                } catch (e) {
                    console.debug('fetchMe: Failed to save session', e);
                }
            } else if (!hadCache) {
                setIsLoggedIn(false);
                try { sessionStorage.removeItem('auth.user'); } catch (e) { console.debug('fetchMe: Failed to remove session', e); }
            }
        } catch (e) {
            console.debug('fetchMe: Call failed', e);
            if (!hadCache) {
                setIsLoggedIn(false);
            }
        }
    }, [applyCachedUser]);

    useEffect(() => {
        applyCachedUser();
        fetchMe();
    }, [location.pathname, fetchMe, applyCachedUser]);

    useEffect(() => {
        const handler = (evt: Event) => {
            try {
                const detailUser = (evt as CustomEvent).detail?.user;
                if (detailUser) {
                    sessionStorage.setItem('auth.user', JSON.stringify(detailUser));
                    setIsLoggedIn(true);
                } else {
                    sessionStorage.removeItem('auth.user');
                    setIsLoggedIn(false);
                }
            } catch (e) {
                console.debug('auth-changed handler error', e);
            } finally {
                fetchMe();
            }
        };
        window.addEventListener('auth-changed', handler as EventListener);
        return () => window.removeEventListener('auth-changed', handler as EventListener);
    }, [fetchMe]);

    const handleLogout = async () => {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
            });
        } catch (e) {
            console.error('logout error', e);
        } finally {
            try { sessionStorage.removeItem('auth.user'); } catch (e) { console.debug('logout: Failed to remove session', e); }
            setIsLoggedIn(false);
            const evt = new CustomEvent('auth-changed', { detail: { user: null } });
            window.dispatchEvent(evt);
            navigate('/');
        }
    };

    return (
        <header className="bg-white border-b border-gray-200">
            <div className="mx-auto w-full max-w-7xl px-10">
                <div className="h-8 flex justify-end items-center space-x-4 text-xs text-gray-500">
                    {isLoggedIn ? (
                        <>
                            <span onClick={() => navigate('/profile')} className="cursor-pointer hover:text-black">마이페이지</span>
                            <span onClick={handleLogout} className="cursor-pointer hover:text-black">로그아웃</span>
                        </>
                    ) : (
                        <span onClick={() => navigate('/login')} className="cursor-pointer hover:text-black">로그인</span>
                    )}
                </div>

                <div className="h-20 flex items-center justify-between">
                    <div className="flex items-center">
                        <h1 className="text-3xl font-black italic tracking-tighter cursor-pointer "
                            onClick={() => navigate('/')}
                        >SOLEBID</h1>
                    </div>

                    <div className="flex items-center space-x-8">
                        <div className="hidden md:flex items-center space-x-8 font-bold">
                            <span className="text-gray-800 hover:text-black cursor-pointer"
                                  style={{ fontSize: '1.125rem' }}
                                  onClick={() => navigate("/")}>
                                HOME
                            </span>
                            <span className="text-gray-800 hover:text-black cursor-pointer"
                                  style={{ fontSize: '1.125rem' }}
                                  onClick={() => navigate("/brand")}>
                                BRAND
                            </span>
                            <span className="text-gray-800 hover:text-black cursor-pointer"
                                  style={{ fontSize: '1.125rem' }}
                                  onClick={() => navigate("/auction")}>
                                AUCTION
                            </span>
                        </div>

                        <div className="flex items-center space-x-5">
                            <button onClick={() => navigate('/search')} className="text-gray-700 hover:text-black" style={{ fontSize: '1.5rem' }}>
                                <i className="fas fa-search"></i>
                            </button>

                            {isLoggedIn && (
                                <>
                                    <button onClick={() => navigate('/wish')} className="text-gray-700 hover:text-black" style={{ fontSize: '1.5rem' }} aria-label="위시리스트">
                                        <i className="fas fa-heart"></i>
                                    </button>
                                    <button onClick={() => navigate('/notifications')} className="text-gray-700 hover:text-black" style={{ fontSize: '1.5rem' }} aria-label="알림">
                                        <i className="fas fa-bell"></i>
                                    </button>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </header>
    );
}

export default AppHeader;

