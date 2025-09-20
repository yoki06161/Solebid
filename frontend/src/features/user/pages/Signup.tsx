import React from "react";
import { Link } from "react-router-dom";
import { useSignup } from "../hooks/UseSignup";
import TermsModal from "../components/TermsModal";
import VerificationCodeInput from "../components/VerificationCodeInput";

const Signup: React.FC = () => {
    const {
        formData, agreements, errors,
        showTermsModal, showPrivacyModal, submitting,
        sendingCode, verificationSent, showSuccessMessage, emailVerified, verifyingCode,
        isFormValid,
        handleInputChange, handleAllAgreements, handleAgreementChange, handleSubmit,
        openTermsModal, openPrivacyModal, closeModal,
        handleKakaoStart, handleGoogleStart, handleSendVerificationCode,
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
                            <div className="flex gap-2">
                                <input
                                    type="email" id="email" name="email"
                                    value={formData.email} onChange={handleInputChange}
                                    disabled={emailVerified}
                                    className={`flex-1 px-4 py-3 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${
                                        emailVerified 
                                            ? 'bg-gray-100 border-gray-300 text-gray-600' 
                                            : errors.email 
                                                ? 'border-red-500 focus:ring-red-500' 
                                                : 'border-gray-300 focus:ring-blue-500'
                                    }`}
                                    placeholder="example@email.com"
                                />
                                {!emailVerified ? (
                                    <button
                                        type="button"
                                        onClick={handleSendVerificationCode}
                                        disabled={!formData.email || !!errors.email || sendingCode}
                                        className={`px-4 py-3 text-sm font-medium rounded-lg whitespace-nowrap transition-colors ${
                                            !formData.email || !!errors.email || sendingCode
                                                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                                : 'bg-blue-600 text-white hover:bg-blue-700'
                                        }`}
                                    >
                                        {sendingCode ? '전송 중...' : '인증번호 받기'}
                                    </button>
                                ) : (
                                    <div className="px-4 py-3 bg-green-100 text-green-700 text-sm font-medium rounded-lg flex items-center">
                                        <i className="fas fa-check-circle mr-2"></i>
                                        인증완료
                                    </div>
                                )}
                            </div>
                            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
                            {showSuccessMessage && !emailVerified && (
                                <p className="text-green-600 text-xs mt-1">
                                    인증번호가 전송되었습니다. 이메일을 확인 후 아래에 입력해주세요.
                                </p>
                            )}
                        </div>
                        
                        {/* 인증번호 입력 (이메일 전송 후에만 표시) */}
                        {verificationSent && !emailVerified && (
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    인증번호 입력 <span className="text-red-500">*</span>
                                </label>
                                <VerificationCodeInput
                                    value={formData.verificationCode || ''}
                                    onChange={(code) => handleInputChange({ target: { name: 'verificationCode', value: code } } as any)}
                                    error={!!errors.verificationCode}
                                />
                                {errors.verificationCode && (
                                    <p className="text-red-500 text-xs mt-1 text-center">{errors.verificationCode}</p>
                                )}
                                {verifyingCode && (
                                    <div className="text-center mt-2">
                                        <div className="inline-flex items-center">
                                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mr-2"></div>
                                            <span className="text-sm text-gray-600">인증 중...</span>
                                        </div>
                                    </div>
                                )}
                                <div className="text-center mt-2">
                                    <p className="text-xs text-gray-500">
                                        인증번호는 5분 후 만료됩니다
                                    </p>
                                </div>
                            </div>
                        )}
                        
                        {/* 이메일 인증 완료 후에만 나머지 필드 표시 */}
                        {emailVerified && (
                            <>
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
                                    <div className="relative">
                                        <input
                                            type="tel" id="phone" name="phone"
                                            value={formData.phone} onChange={handleInputChange}
                                            className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:border-blue-500 text-sm ${errors.phone ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'}`}
                                            placeholder="전화번호를 입력하세요 (자동으로 하이픈 추가됩니다)"
                                            maxLength={13}
                                        />
                                        {/* 전화번호 아이콘 */}
                                        <div className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400">
                                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                                            </svg>
                                        </div>
                                    </div>
                                    {errors.phone && <p className="text-red-500 text-xs mt-1">{errors.phone}</p>}
                                </div>
                            </>
                        )}
                        {/* Terms Agreement - 이메일 인증 완료 후에만 표시 */}
                        {emailVerified && (
                            <>
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
                            </>
                        )}
                        {/* Divider */}
                        <div className="relative my-6">
                            <div className="absolute inset-0 flex items-center">
                                <div className="w-full border-t border-gray-300"></div>
                            </div>
                            <div className="relative flex justify-center text-sm">
                                <span className="px-2 bg-white text-gray-500">또는</span>
                            </div>
                        </div>
                        {/* Social Login Buttons */}
                        <div className="space-y-3">
                            {/* Kakao Signup */}
                            <button type="button" onClick={handleKakaoStart}
                                    className="w-full py-3 bg-[#FEE500] text-gray-900 font-medium !rounded-button hover:bg-[#FDD800] cursor-pointer whitespace-nowrap flex items-center justify-center transition-colors no-underline">
                                <i className="fas fa-comment mr-2"></i>
                                카카오로 시작하기
                            </button>
                            {/* Google Signup */}
                            <button type="button" onClick={handleGoogleStart}
                    className="w-full py-3 bg-white border border-gray-300 text-gray-700 font-medium !rounded-button hover:bg-gray-50 cursor-pointer whitespace-nowrap flex items-center justify-center transition-colors no-underline">
                                <i
                        className="fab fa-google mr-2"
                        style={{
                            background:
                            'conic-gradient(from -45deg, #ea4335 110deg, #4285f4 110deg 200deg, #34a853 200deg 290deg, #fbbc05 290deg)',
                            WebkitBackgroundClip: 'text',
                            WebkitTextFillColor: 'transparent',
                            }}
                        />
                                구글로 시작하기
                            </button>
                        </div>
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
