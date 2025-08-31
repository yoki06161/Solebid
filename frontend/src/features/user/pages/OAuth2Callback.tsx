import React from 'react';
import { useOAuth2Callback } from '../hooks/UseOAuth2Callback';

const OAuth2Callback: React.FC = () => {
  const { status, message, provider, retry } = useOAuth2Callback();

  const getProviderName = (p: string) => {
    switch (p) {
      case 'google':
        return '구글';
      case 'kakao':
        return '카카오';
      default:
        return p;
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
              onClick={retry}
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