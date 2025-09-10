import React, { useState } from 'react';
import { requestPasswordReset } from '../services/PasswordResetService';
import type { ApiResponse } from '../types/AuthTypes';

const FindPassword: React.FC = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      setError('이메일을 입력하세요');
      return;
    }
    setLoading(true);
    setMessage(null);
    setError(null);
    try {
      const res: ApiResponse = await requestPasswordReset(email.trim());
      if (res.success) {
        setMessage('비밀번호 재설정 메일을 발송했습니다. (15분 이내 유효)');
      } else {
        setError(res.message || '요청 실패');
      }
    } catch (e: any) {
      setError(e.message || '요청 중 오류');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4">
      <div className="bg-white shadow-lg rounded-lg w-full max-w-md p-8">
        <h1 className="text-2xl font-bold mb-2 text-gray-800">비밀번호 찾기</h1>
        <p className="text-sm text-gray-500 mb-6">가입한 이메일로 재설정 링크를 보내드립니다.</p>
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">이메일</label>
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="example@domain.com"
              disabled={loading}
            />
          </div>
          {error && <div className="text-xs text-red-500">{error}</div>}
          {message && <div className="text-xs text-green-600">{message}</div>}
          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-blue-600 text-white font-medium !rounded-button hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center text-sm"
          >
            {loading ? '전송 중...' : '재설정 메일 보내기'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default FindPassword;

