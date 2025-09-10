import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { submitPasswordReset } from '../services/PasswordResetService';
import type { ApiResponse } from '../types/AuthTypes';

const ResetPassword: React.FC = () => {
  const [params] = useSearchParams();
  const token = params.get('token') || '';
  const [pw, setPw] = useState('');
  const [pw2, setPw2] = useState('');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) setError('토큰이 없습니다. 메일 링크를 다시 확인하세요.');
  }, [token]);

  const validate = (): string | null => {
    if (!pw || pw.length < 8) return '비밀번호는 8자 이상이어야 합니다.';
    if (pw.length > 64) return '비밀번호는 64자 이하여야 합니다.';
    if (pw !== pw2) return '비밀번호가 일치하지 않습니다.';
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (loading || done) return;
    const v = validate();
    if (v) { setError(v); return; }
    setLoading(true);
    setError(null);
    try {
      const res: ApiResponse = await submitPasswordReset(token, pw);
      if (res.success) {
        setDone(true);
      } else {
        setError(res.message || '재설정 실패');
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
        <h1 className="text-2xl font-bold mb-2 text-gray-800">비밀번호 재설정</h1>
        {!done && <p className="text-sm text-gray-500 mb-6">새 비밀번호를 입력하세요.</p>}
        {done ? (
          <div className="space-y-6">
            <div className="text-green-600 text-sm">비밀번호가 재설정되었습니다. 이제 로그인할 수 있습니다.</div>
            <Link to="/login" className="w-full block text-center py-3 bg-blue-600 text-white font-medium !rounded-button hover:bg-blue-700 text-sm">로그인 페이지로 이동</Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">새 비밀번호</label>
              <input
                type="password"
                value={pw}
                onChange={e => setPw(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="8자 이상"
                disabled={loading || !token}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">새 비밀번호 확인</label>
              <input
                type="password"
                value={pw2}
                onChange={e => setPw2(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="다시 입력"
                disabled={loading || !token}
              />
            </div>
            {error && <div className="text-xs text-red-500">{error}</div>}
            <button
              type="submit"
              disabled={loading || !token}
              className="w-full py-3 bg-blue-600 text-white font-medium !rounded-button hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center text-sm"
            >
              {loading ? '처리 중...' : '비밀번호 변경'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default ResetPassword;

