import React from 'react';
import { useNicknameSetup } from '../hooks/UseNicknameSetup';

const NicknameSetup: React.FC = () => {
  const { nickname, setNickname, available, checking, submitting, error, setError, isBasicValid, submit } = useNicknameSetup();

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-6">
      <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <i className="fas fa-user-edit text-2xl text-blue-600"></i>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-2">닉네임 설정</h3>
          <p className="text-gray-600 text-sm">다른 사용자와 구분될 고유한 닉네임을 입력하세요.</p>
        </div>
        <form onSubmit={submit} className="space-y-6">
          <div>
            <label htmlFor="nickname" className="block text-sm font-medium text-gray-700 mb-2">
              닉네임
            </label>
            <div className="relative">
              <input
                id="nickname"
                type="text"
                value={nickname}
                onChange={(e) => { setNickname(e.target.value); setError(null); }}
                placeholder="2~10자, 영문/숫자/한글"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                maxLength={10}
                autoFocus
              />
              {checking && (
                <div className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm">
                  <i className="fas fa-spinner fa-spin" />
                </div>
              )}
            </div>
            {available === true && isBasicValid && (
              <p className="text-green-600 text-xs mt-1">사용 가능한 닉네임입니다.</p>
            )}
            {available === false && (
              <p className="text-red-500 text-xs mt-1">이미 사용 중인 닉네임입니다.</p>
            )}
            {!isBasicValid && nickname.length > 0 && (
              <p className="text-red-500 text-xs mt-1">닉네임은 2~10자이며 user_로 시작할 수 없습니다.</p>
            )}
            {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
          </div>
          <button
            type="submit"
            disabled={submitting || !isBasicValid || available === false}
            className="w-full py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer transition-colors"
          >
            {submitting ? (<><i className="fas fa-spinner fa-spin mr-2" />설정 중...</>) : '닉네임 설정 완료'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default NicknameSetup;
