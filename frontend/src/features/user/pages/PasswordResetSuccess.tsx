import React from 'react';
import { Link } from 'react-router-dom';

const PasswordResetSuccess: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4">
      <div className="bg-white shadow-lg rounded-lg w-full max-w-md p-8 text-center">
        {/* 성공 아이콘 */}
        <div className="mx-auto w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mb-6">
          <svg 
            className="w-8 h-8 text-green-600" 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path 
              strokeLinecap="round" 
              strokeLinejoin="round" 
              strokeWidth={2} 
              d="M5 13l4 4L19 7" 
            />
          </svg>
        </div>

        {/* 제목 */}
        <h1 className="text-2xl font-bold mb-4 text-gray-800">
          비밀번호 재설정 완료
        </h1>

        {/* 설명 */}
        <p className="text-gray-600 mb-8 leading-relaxed">
          비밀번호가 성공적으로 변경되었습니다.<br />
          새로운 비밀번호로 로그인해 주세요.
        </p>

        {/* 로그인 버튼 */}
        <Link
          to="/login"
          className="w-full inline-block py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors text-sm"
        >
          로그인 페이지로 이동
        </Link>

        {/* 추가 안내 */}
        <div className="mt-6 pt-6 border-t border-gray-200">
          <p className="text-xs text-gray-500">
            보안을 위해 기존 로그인 세션이 모두 종료되었습니다.
          </p>
        </div>
      </div>
    </div>
  );
};

export default PasswordResetSuccess;