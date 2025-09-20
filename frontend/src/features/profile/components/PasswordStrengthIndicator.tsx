import React from 'react';

interface PasswordStrengthIndicatorProps {
  password: string;
  className?: string;
}

interface StrengthCriteria {
  label: string;
  test: (password: string) => boolean;
}

const PasswordStrengthIndicator: React.FC<PasswordStrengthIndicatorProps> = ({ 
  password, 
  className = '' 
}) => {
  const criteria: StrengthCriteria[] = [
    {
      label: '8자 이상',
      test: (pwd) => pwd.length >= 8,
    },
    {
      label: '영문 소문자 포함',
      test: (pwd) => /[a-z]/.test(pwd),
    },
    {
      label: '영문 대문자 포함',
      test: (pwd) => /[A-Z]/.test(pwd),
    },
    {
      label: '숫자 포함',
      test: (pwd) => /\d/.test(pwd),
    },
    {
      label: '특수문자 포함',
      test: (pwd) => /[@$!%*?&]/.test(pwd),
    },
  ];

  const passedCriteria = criteria.filter(criterion => criterion.test(password));
  const strengthPercentage = (passedCriteria.length / criteria.length) * 100;

  const getStrengthLevel = () => {
    if (strengthPercentage === 0) return { level: 'none', text: '', color: 'bg-gray-200' };
    if (strengthPercentage <= 40) return { level: 'weak', text: '약함', color: 'bg-red-500' };
    if (strengthPercentage <= 80) return { level: 'medium', text: '보통', color: 'bg-yellow-500' };
    return { level: 'strong', text: '강함', color: 'bg-green-500' };
  };

  const strength = getStrengthLevel();

  if (!password) {
    return null;
  }

  return (
    <div className={`space-y-3 ${className}`}>
      {/* 강도 표시 바 */}
      <div className="space-y-2">
        <div className="flex justify-between items-center">
          <span className="text-sm font-medium text-gray-700">비밀번호 강도</span>
          {strength.text && (
            <span className={`text-sm font-medium ${
              strength.level === 'weak' ? 'text-red-600' :
              strength.level === 'medium' ? 'text-yellow-600' :
              'text-green-600'
            }`}>
              {strength.text}
            </span>
          )}
        </div>
        <div className="w-full bg-gray-200 rounded-full h-2">
          <div
            className={`h-2 rounded-full transition-all duration-300 ${strength.color}`}
            style={{ width: `${strengthPercentage}%` }}
          />
        </div>
      </div>

      {/* 조건 체크리스트 */}
      <div className="space-y-1">
        <p className="text-xs text-gray-600 mb-2">비밀번호 조건:</p>
        {criteria.map((criterion, index) => {
          const isPassed = criterion.test(password);
          return (
            <div key={index} className="flex items-center space-x-2">
              <div className={`w-4 h-4 rounded-full flex items-center justify-center ${
                isPassed ? 'bg-green-100' : 'bg-gray-100'
              }`}>
                {isPassed ? (
                  <svg className="w-3 h-3 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                ) : (
                  <div className="w-2 h-2 bg-gray-400 rounded-full" />
                )}
              </div>
              <span className={`text-xs ${
                isPassed ? 'text-green-600' : 'text-gray-500'
              }`}>
                {criterion.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default PasswordStrengthIndicator;