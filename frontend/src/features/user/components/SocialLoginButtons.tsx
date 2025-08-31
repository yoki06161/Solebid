import React from 'react';
import type { Provider } from '../types/AuthTypes.ts';

type Props = {
  isLoading?: boolean;
  onSocialLogin: (provider: Provider) => void;
};

const SocialLoginButtons: React.FC<Props> = ({ isLoading, onSocialLogin }) => {
  return (
    <div className="space-y-3">
      <button
        type="button"
        onClick={() => onSocialLogin('kakao')}
        disabled={!!isLoading}
        className="w-full py-3 bg-[#FEE500] text-gray-900 font-medium !rounded-button hover:bg-[#FDD800] disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer whitespace-nowrap flex items-center justify-center transition-colors"
      >
        <i className="fas fa-comment mr-2"></i>
        {isLoading ? '처리 중...' : '카카오 로그인'}
      </button>
      <button
        type="button"
        onClick={() => onSocialLogin('google')}
        disabled={!!isLoading}
        className="w-full py-3 bg-white border border-gray-300 text-gray-700 font-medium !rounded-button hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer whitespace-nowrap flex items-center justify-center transition-colors"
      >
        <i
          className="fab fa-google mr-2"
          style={{
            background:
              'conic-gradient(from -45deg, #ea4335 110deg, #4285f4 110deg 200deg, #34a853 200deg 290deg, #fbbc05 290deg)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}
        />
        {isLoading ? '처리 중...' : '구글 로그인'}
      </button>
    </div>
  );
};

export default SocialLoginButtons;
