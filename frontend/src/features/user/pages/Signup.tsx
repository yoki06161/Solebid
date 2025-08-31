import React from "react";
import { Link } from "react-router-dom";
import { useSignup } from "../hooks/UseSignup";
import TermsModal from "../components/TermsModal";

const Signup: React.FC = () => {
    const {
        formData, agreements, errors,
        showTermsModal, showPrivacyModal, submitting,
        isFormValid,
        handleInputChange, handleAllAgreements, handleAgreementChange, handleSubmit,
        openTermsModal, openPrivacyModal, closeModal,
        handleKakaoStart,
    } = useSignup();

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Main Content */}
            <div className="flex items-center justify-center py-12 px-6">
                <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
                    <div className="text-center mb-8">
                        <h3 className="text-2xl font-bold text-gray-900 mb-2">
                            새 계정 만들기
                        </h3>
                        <p className="text-gray-600 text-sm">
                            SoleBid에서 신발 경매를 시작해보세요
                        </p>
                    </div>
                    <form onSubmit={handleSubmit} className="space-y-6">
                        {/* Email */}
                        <div>
                            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                                이메일 <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="email" id="email" name="email"
                                value={formData.email} onChange={handleInputChange}
                                className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${errors.email ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'}`}
                                placeholder="example@email.com"
                            />
                            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
                        </div>
                        {/* Password */}
                        <div>
                            <label htmlFor="password"
                                   className="block text-sm font-medium text-gray-700 mb-2">
                                비밀번호 <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="password" id="password" name="password"
                                value={formData.password} onChange={handleInputChange}
                                className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${errors.password ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'}`}
                                placeholder="8~20자 영문, 숫자, 특수문자 포함"
                            />
                            {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
                        </div>
                        {/* Confirm Password */}
                        <div>
                            <label htmlFor="confirmPassword"
                                   className="block text-sm font-medium text-gray-700 mb-2">
                                비밀번호 확인 <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="password" id="confirmPassword" name="confirmPassword"
                                value={formData.confirmPassword} onChange={handleInputChange}
                                className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${errors.confirmPassword ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'}`}
                                placeholder="비밀번호를 다시 입력해주세요"
                            />
                            {errors.confirmPassword && <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>}
                        </div>
                        {/* Nickname */}
                        <div>
                            <label htmlFor="nickname" className="block text-sm font-medium text-gray-700 mb-2">
                                닉네임 <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text" id="nickname" name="nickname"
                                value={formData.nickname} onChange={handleInputChange}
                                className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${errors.nickname ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'}`}
                                placeholder="2자 이상 10자 이하"
                            />
                            {errors.nickname && <p className="text-red-500 text-xs mt-1">{errors.nickname}</p>}
                        </div>
                        {/* Name */}
                        <div>
                            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
                                이름 <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text" id="name" name="name"
                                value={formData.name} onChange={handleInputChange}
                                className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${errors.name ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'}`}
                                placeholder="실명을 입력해주세요"
                            />
                            {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name}</p>}
                        </div>
                        {/* Phone */}
                        <div>
                            <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-2">
                                전화번호 <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="tel" id="phone" name="phone"
                                value={formData.phone} onChange={handleInputChange}
                                className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${errors.phone ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'}`}
                                placeholder="'-' 없이 숫자만 입력"
                            />
                            {errors.phone && <p className="text-red-500 text-xs mt-1">{errors.phone}</p>}
                        </div>
                        {/* Terms Agreement */}
                        <div className="space-y-3 pt-4">
                            <div className="flex items-center">
                                <input type="checkbox" id="all" checked={agreements.all} onChange={handleAllAgreements}
                                       className="w-4 h-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500" />
                                <label htmlFor="all" className="ml-2 text-sm font-medium text-gray-700">
                                    전체 동의
                                </label>
                            </div>
                            <div className="flex items-start">
                                <input type="checkbox" id="terms" name="terms" checked={agreements.terms} onChange={handleAgreementChange}
                                       className="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded cursor-pointer" />
                                <label htmlFor="terms" className="ml-3 text-sm text-gray-700 cursor-pointer">
                                    <span className="text-red-500">*</span> 이용약관에 동의합니다
                                    <a href="#" onClick={openTermsModal} className="text-blue-600 hover:text-blue-800 ml-1 underline cursor-pointer">
                                        보기
                                    </a>
                                </label>
                            </div>
                            {errors.agreeTerms && <p className="text-red-500 text-xs ml-7">{errors.agreeTerms}</p>}
                            <div className="flex items-start">
                                <input type="checkbox" id="privacy" name="privacy" checked={agreements.privacy} onChange={handleAgreementChange}
                                       className="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded cursor-pointer" />
                                <label htmlFor="privacy" className="ml-3 text-sm text-gray-700 cursor-pointer">
                                    <span className="text-red-500">*</span> 개인정보 처리방침에 동의합니다
                                    <a href="#" onClick={openPrivacyModal} className="text-blue-600 hover:text-blue-800 ml-1 underline cursor-pointer">
                                        보기
                                    </a>
                                </label>
                            </div>
                            {errors.agreePrivacy && <p className="text-red-500 text-xs ml-7">{errors.agreePrivacy}</p>}
                            <div className="flex items-start">
                                <input type="checkbox" id="marketing" name="marketing" checked={agreements.marketing} onChange={handleAgreementChange}
                                       className="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded cursor-pointer" />
                                <label htmlFor="marketing" className="ml-3 text-sm text-gray-700 cursor-pointer">
                                    마케팅 정보 수신에 동의합니다 (선택)
                                </label>
                            </div>
                        </div>
                        {/* Submit Button */}
                        <button type="submit" disabled={!isFormValid || submitting}
                                className={`w-full py-3 font-medium !rounded-button cursor-pointer whitespace-nowrap transition-colors ${isFormValid && !submitting ? 'bg-blue-500 text-white hover:bg-blue-600' : 'bg-gray-300 text-gray-500 cursor-not-allowed'}`}>
                            {submitting ? '처리 중...' : '회원가입'}
                        </button>
                        {/* Divider */}
                        <div className="relative my-6">
                            <div className="absolute inset-0 flex items-center">
                                <div className="w-full border-t border-gray-300"></div>
                            </div>
                            <div className="relative flex justify-center text-sm">
                                <span className="px-2 bg-white text-gray-500">또는</span>
                            </div>
                        </div>
                        {/* Kakao Signup */}
                        <button type="button" onClick={handleKakaoStart}
                                className="w-full py-3 bg-[#FEE500] text-gray-900 font-medium !rounded-button hover:bg-[#FDD800] cursor-pointer whitespace-nowrap flex items-center justify-center transition-colors no-underline">
                            <i className="fas fa-comment mr-2"></i>
                            카카오로 시작하기
                        </button>
                    </form>
                    {/* Login Link */}
                    <div className="text-center mt-8 pt-6 border-t border-gray-200">
                        <span className="text-gray-600 text-sm">이미 계정이 있으신가요?</span>
                        <Link to="/login" className="ml-2 text-blue-600 hover:text-blue-800 text-sm font-medium cursor-pointer">
                            로그인
                        </Link>
                    </div>
                </div>
            </div>
            {/* Terms Modal */}
            <TermsModal open={showTermsModal} onClose={closeModal} title="이용약관">
                <p>이용약관 내용이 여기에 표시됩니다.</p>
            </TermsModal>
            {/* Privacy Modal */}
            <TermsModal open={showPrivacyModal} onClose={closeModal} title="개인정보 처리방침">
                <p>개인정보 처리방침 내용이 여기에 표시됩니다.</p>
            </TermsModal>
        </div>
    );
};

export default Signup;
