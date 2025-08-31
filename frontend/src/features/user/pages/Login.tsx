import React from "react";
import { Link } from "react-router-dom";
import SocialLoginButtons from "../components/SocialLoginButtons";
import { useEmailLogin } from "../hooks/UseEmailLogin";

const Login: React.FC = () => {
    const { loginForm, errors, isLoading, handleInputChange, handleSubmit, handleSocialLogin } = useEmailLogin();

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
                    <form onSubmit={handleSubmit} className="space-y-6">
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
                    {/* 소셜 로그인 버튼 분리 */}
                    <SocialLoginButtons isLoading={isLoading} onSocialLogin={handleSocialLogin} />
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