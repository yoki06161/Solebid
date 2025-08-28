import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";

const Login: React.FC = () => {
    const navigate = useNavigate();
    const [loginForm, setLoginForm] = useState({
        email: "",
        password: "",
    });
    const [errors, setErrors] = useState<{ [key: string]: string }>({});
    const [isLoading, setIsLoading] = useState(false);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setLoginForm((prev) => ({
            ...prev,
            [name]: value,
        }));
        if (errors[name] || errors.submit) {
            setErrors((prev) => ({
                ...prev,
                [name]: "",
                submit: ""
            }));
        }
    };

    const validateForm = () => {
        const newErrors: { [key: string]: string } = {};
        if (!loginForm.email.trim()) {
            newErrors.email = "이메일을 입력해주세요.";
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(loginForm.email)) {
            newErrors.email = "올바른 이메일 형식을 입력해주세요.";
        }
        if (!loginForm.password.trim()) {
            newErrors.password = "비밀번호를 입력해주세요.";
        }
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleReactivate = async (token: string) => {
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
                } catch (e) { console.debug('reactivate cache fail', e); }
                navigate('/');
                return true;
            }
            setErrors({ submit: data?.message || '계정 재활성화에 실패했습니다.' });
            return false;
        } catch (e) {
            console.error('reactivate error', e);
            setErrors({ submit: '네트워크 오류가 발생했습니다. 다시 시도해주세요.' });
            return false;
        }
    };

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) {
            return;
        }
        setIsLoading(true);
        try {
            const response = await fetch(
                "/api/users/login",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    credentials: 'include', // 쿠키 포함
                    body: JSON.stringify(loginForm),
                },
            );
            const result = await response.json();

            if (response.ok && result.success) {
                setErrors({});
                if (result.data) {
                    try {
                        sessionStorage.setItem('auth.user', JSON.stringify(result.data));
                    } catch (e) {
                        console.debug('auth.user 세션 저장 실패', e);
                    }
                }
                const evt = new CustomEvent('auth-changed', { detail: { user: result.data || null } });
                window.dispatchEvent(evt);
                navigate("/");
            } else if (result?.errorCode === 'WITHDRAWN_USER' && result?.data?.reactivationToken) {
                const ok = window.confirm('회원 탈퇴 처리된 계정입니다. 계정을 다시 활성화하시겠습니까?');
                if (ok) {
                    await handleReactivate(String(result.data.reactivationToken));
                } else {
                    setErrors({ submit: '재활성화가 취소되었습니다.' });
                }
            } else {
                setErrors({
                    submit: result.message || "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.",
                });
            }
        } catch (error) {
            console.error("Login error:", error);
            setErrors({ submit: "네트워크 오류가 발생했습니다. 다시 시도해주세요." });
        } finally {
            setIsLoading(false);
        }
    };

    // 소셜 로그인 처리
    const handleSocialLogin = async (provider: 'google' | 'kakao') => {
        try {
            setIsLoading(true);

            // 1. OAuth2 인증 URL 요청
            const response = await fetch(`/api/auth/oauth2/${provider}/url`, {
                credentials: 'include' // 쿠키 포함
            });
            const data = await response.json();

            if (data.success) {
                // 2. 소셜 플랫폼 인증 페이지로 이동
                window.location.href = data.data.authUrl;
            } else {
                setErrors({
                    submit: data.message || `${provider} 로그인 URL 생성에 실패했습니다.`
                });
                setIsLoading(false);
            }
        } catch (error) {
            console.error(`${provider} 로그인 오류:`, error);
            setErrors({
                submit: `${provider} 로그인 중 오류가 발생했습니다.`
            });
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="flex items-center justify-center py-12 px-6">
                <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
                    <div className="text-center mb-8">
                        <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <i className="fas fa-envelope text-2xl text-blue-600"></i>
                        </div>
                        <h3 className="text-2xl font-bold text-gray-900 mb-2">로그인</h3>
                        <p className="text-gray-600 text-sm">계정으로 로그인하세요</p>
                    </div>
                    <form onSubmit={handleLogin} className="space-y-6">
                        <div>
                            <label
                                htmlFor="email"
                                className="block text-sm font-medium text-gray-700 mb-2"
                            >
                                이메일 주소
                            </label>
                            <div className="relative">
                                <input
                                    type="email"
                                    id="email"
                                    name="email"
                                    value={loginForm.email}
                                    onChange={handleInputChange}
                                    className="w-full px-4 py-3 pl-12 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                                    placeholder="이메일 주소를 입력하세요"
                                />
                                <div className="absolute left-4 top-1/2 transform -translate-y-1/2">
                                    <i className="fas fa-envelope text-gray-400 text-sm"></i>
                                </div>
                            </div>
                            {errors.email && (
                                <p className="text-red-500 text-xs mt-1">{errors.email}</p>
                            )}
                        </div>
                        <div>
                            <label
                                htmlFor="password"
                                className="block text-sm font-medium text-gray-700 mb-2"
                            >
                                비밀번호
                            </label>
                            <div className="relative">
                                <input
                                    type="password"
                                    id="password"
                                    name="password"
                                    value={loginForm.password}
                                    onChange={handleInputChange}
                                    className="w-full px-4 py-3 pl-12 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                                    placeholder="비밀번호를 입력하세요"
                                />
                                <div className="absolute left-4 top-1/2 transform -translate-y-1/2">
                                    <i className="fas fa-lock text-gray-400 text-sm"></i>
                                </div>
                            </div>
                            {errors.password && (
                                <p className="text-red-500 text-xs mt-1">{errors.password}</p>
                            )}
                            {errors.submit && (
                                <p className="text-red-500 text-xs mt-1">{errors.submit}</p>
                            )}
                        </div>
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full py-3 bg-blue-600 text-white font-medium !rounded-button hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer whitespace-nowrap flex items-center justify-center transition-colors"
                        >
                            {isLoading ? (
                                <>
                                    <i className="fas fa-spinner fa-spin mr-2"></i>
                                    로그인 중...
                                </>
                            ) : (
                                <>
                                    <i className="fas fa-sign-in-alt mr-2"></i>
                                    로그인
                                </>
                            )}
                        </button>
                    </form>
                    <div className="text-center mt-6">
                        <Link
                            to="/find-password"
                            className="text-gray-600 hover:text-gray-900 text-sm font-medium cursor-pointer transition-colors"
                        >
                            비밀번호를 잊으셨나요?
                        </Link>
                    </div>
                    <div className="relative my-8">
                        <div className="absolute inset-0 flex items-center">
                            <div className="w-full border-t border-gray-300"></div>
                        </div>
                        <div className="relative flex justify-center text-sm">
                            <span className="px-2 bg-white text-gray-500">또는</span>
                        </div>
                    </div>
                    <div className="space-y-3">
                        <button
                            type="button"
                            onClick={() => handleSocialLogin('kakao')}
                            disabled={isLoading}
                            className="w-full py-3 bg-[#FEE500] text-gray-900 font-medium !rounded-button hover:bg-[#FDD800] disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer whitespace-nowrap flex items-center justify-center transition-colors"
                        >
                            <i className="fas fa-comment mr-2"></i>
                            {isLoading ? '처리 중...' : '카카오 로그인'}
                        </button>
                        <button
                            type="button"
                            onClick={() => handleSocialLogin('google')}
                            disabled={isLoading}
                            className="w-full py-3 bg-white border border-gray-300 text-gray-700 font-medium !rounded-button hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer whitespace-nowrap flex items-center justify-center transition-colors"
                        >
                            <i
                                className="fab fa-google mr-2"
                                style={{
                                    background:
                                        "conic-gradient(from -45deg, #ea4335 110deg, #4285f4 110deg 200deg, #34a853 200deg 290deg, #fbbc05 290deg)",
                                    WebkitBackgroundClip: "text",
                                    WebkitTextFillColor: "transparent",
                                }}
                            ></i>
                            {isLoading ? '처리 중...' : '구글 로그인'}
                        </button>
                    </div>
                    <div className="text-center mt-8 pt-6 border-t border-gray-200">
                        <span className="text-gray-600 text-sm">
                            아직 계정이 없으신가요?
                        </span>
                        <Link
                            to="/signup"
                            className="ml-2 text-blue-600 hover:text-blue-800 text-sm font-medium cursor-pointer"
                        >
                            회원가입
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};
export default Login;