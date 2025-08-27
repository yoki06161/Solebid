import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";

function MainHeader() {
    const navigate = useNavigate();
    const location = useLocation();
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [showProfileMenu, setShowProfileMenu] = useState(false);
    const [userProfile, setUserProfile] = useState({
        name: "사용자", // 기본값
    });

    const applyCachedUser = () => {
        try {
            const raw = sessionStorage.getItem('auth.user');
            if (raw) {
                const cached = JSON.parse(raw);
                if (cached && (cached.nickname || cached.email || cached.userId)) {
                    setIsLoggedIn(true);
                    setUserProfile({ name: cached.nickname || "사용자" });
                    return true;
                }
            }
        } catch (e) {
            console.debug('applyCachedUser: 세션 파싱 실패', e);
        }
        return false;
    };

    const fetchMe = async () => {
        // 캐시를 먼저 반영
        const hadCache = applyCachedUser();
        try {
            const res = await fetch('/api/users/me', { credentials: 'include' });
            const data = await res.json();
            if (res.ok && data.success) {
                const user = data.data || null;
                setIsLoggedIn(true);
                setUserProfile({ name: user?.nickname || "사용자" });
                try { sessionStorage.setItem('auth.user', JSON.stringify(user));
                } catch (e) {
                    console.debug('fetchMe: 세션 저장 실패', e);
                }
            } else if (!hadCache) {
                setIsLoggedIn(false);
                setUserProfile({ name: "사용자" });
                try { sessionStorage.removeItem('auth.user'); } catch (e) { console.debug('fetchMe: 세션 제거 실패', e); }
            }
        } catch (e) {
            console.debug('fetchMe: 호출 실패', e);
            if (!hadCache) {
                setIsLoggedIn(false);
                setUserProfile({ name: "사용자" });
            }
        }
    };

    // 최초 마운트 및 라우트 변경 시 재검증(+즉시 캐시 반영)
    useEffect(() => {
        applyCachedUser();
        fetchMe();
    }, [location.pathname]);

    // 로그인/로그아웃 등 인증 상태 변경 이벤트 수신 (즉시 반영 후 백그라운드 검증)
    useEffect(() => {
        const handler = (evt: Event) => {
            try {
                const detailUser = (evt as CustomEvent).detail?.user;
                if (detailUser) {
                    sessionStorage.setItem('auth.user', JSON.stringify(detailUser));
                    setIsLoggedIn(true);
                    setUserProfile({ name: detailUser.nickname || "사용자" });
                } else {
                    sessionStorage.removeItem('auth.user');
                    setIsLoggedIn(false);
                    setUserProfile({ name: "사용자" });
                }
            } catch (e) {
                console.debug('auth-changed 핸들러 오류', e);
            } finally {
                // 서버 상태 확인 (백그라운드)
                fetchMe();
            }
        };
        window.addEventListener('auth-changed', handler as EventListener);
        return () => window.removeEventListener('auth-changed', handler as EventListener);
    }, []);

    const handleLogout = async () => {
        try {
            // 서버에 로그아웃 요청하여 HttpOnly 쿠키 제거
            await fetch('/api/auth/logout', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
            });
        } catch (e) {
            console.error('logout error', e);
        } finally {
            try { sessionStorage.removeItem('auth.user'); } catch (e) { console.debug('logout: 세션 제거 실패', e); }
            setIsLoggedIn(false);
            setShowProfileMenu(false);
            // 상태 변화 이벤트 브로드캐스트(로그아웃 알림)
            const evt = new CustomEvent('auth-changed', { detail: { user: null } });
            window.dispatchEvent(evt);
            navigate('/');
        }
    };

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            const profileMenu = document.getElementById("profile-menu");
            const profileButton = document.getElementById("profile-button");
            if (
                profileMenu &&
                profileButton &&
                !profileMenu.contains(event.target as Node) &&
                !profileButton.contains(event.target as Node)
            ) {
                setShowProfileMenu(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    return (
        <nav className="bg-white shadow-sm">
            <div className="max-w-[1440px] mx-auto px-6 h-16 flex items-center justify-between">
                <div className="flex items-center space-x-8">
                    <h1 className="text-2xl font-bold text-blue-600 cursor-pointer"
                        onClick={() => navigate('/')}
                    >SoleBid</h1>
                    <div className="hidden md:flex space-x-6">
                        <span className="text-gray-600 hover:text-gray-900 cursor-pointer"
                            onClick={() => navigate("/auction")}
                        >경매</span>
                        <span className="text-gray-600 hover:text-gray-900 cursor-pointer"
                            onClick={() => navigate("/brand")}
                        >브랜드</span>
                        <span className="text-gray-600 hover:text-gray-900 cursor-pointer"
                            onClick={() => navigate("/ranking")}
                        >랭킹</span>
                    </div>
                </div>

                <div className="flex items-center space-x-4">
                    <div className="flex items-center">
                        <div className="relative">
                            <input
                                type="search"
                                placeholder="상품 검색"
                                className="w-64 pl-10 pr-4 py-2 bg-gray-100 rounded-l-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                            <i className="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-sm"></i>
                        </div>
                        <button
                            className="px-4 py-2 bg-blue-500 text-white !rounded-r-lg hover:bg-blue-600 cursor-pointer whitespace-nowrap"
                            onClick={() => navigate("/search")}>
                            검색
                        </button>
                    </div>
                    <button
                        className="px-4 py-2 bg-blue-500 text-white !rounded-button hover:bg-blue-600 cursor-pointer whitespace-nowrap"
                        onClick={() => navigate(`/bid`)}
                    >
                        경매 등록
                    </button>

                    {isLoggedIn ? (
                        <div className="relative">
                            <button
                                id="profile-button"
                                onClick={() => setShowProfileMenu(!showProfileMenu)}
                                className="flex items-center space-x-2 px-4 py-2 border border-gray-300 text-gray-700 !rounded-button hover:bg-gray-50"
                            >
                                <i className="fas fa-user-circle text-lg"></i>
                                <span>{userProfile.name}</span>
                            </button>
                            <div
                                id="profile-menu"
                                className={`absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg transition-all duration-200 z-50 ${showProfileMenu ? "opacity-100 visible" : "opacity-0 invisible"
                                    }`}
                            >
                                <button onClick={() => navigate('/profile')}
                                    className="w-full text-left block px-4 py-2 text-gray-700 hover:bg-gray-50">
                                    <i className="fas fa-user mr-2"></i>마이페이지
                                </button>
                                <button onClick={() => navigate('/settings')}
                                    className="w-full text-left block px-4 py-2 text-gray-700 hover:bg-gray-50">
                                    <i className="fas fa-cog mr-2"></i>설정
                                </button>
                                <button
                                    onClick={handleLogout}
                                    className="w-full text-left px-4 py-2 text-red-600 hover:bg-gray-50"
                                >
                                    <i className="fas fa-sign-out-alt mr-2"></i>로그아웃
                                </button>
                            </div>
                        </div>
                    ) : (
                        <>
                            <button
                                onClick={() => navigate('/login')}
                                className="px-4 py-2 border border-gray-300 text-gray-700 !rounded-button hover:bg-gray-50 cursor-pointer whitespace-nowrap"
                            >
                                로그인
                            </button>
                            <button
                                onClick={() => navigate('/signup')}
                                className="px-4 py-2 bg-gray-900 text-white !rounded-button hover:bg-gray-800 cursor-pointer whitespace-nowrap">
                                회원가입
                            </button>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
}

export default MainHeader;
