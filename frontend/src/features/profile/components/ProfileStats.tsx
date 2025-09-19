import { useState, useEffect, useCallback } from 'react';
import TemperatureCard from './TemperatureCard';
import { fetchUserProfile } from '../api/profile';

const ProfileStats = () => {
    const [temperature, setTemperature] = useState<number>(36.5); // 기본값
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [retryCount, setRetryCount] = useState<number>(0);

    const loadUserProfile = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            
            const profile = await fetchUserProfile();
            setTemperature(profile.temperature);
            setRetryCount(0); // 성공 시 재시도 카운트 리셋
        } catch (err) {
            console.error('Failed to fetch user profile:', err);
            const errorMessage = err instanceof Error ? err.message : '온도 정보를 불러올 수 없습니다.';
            setError(errorMessage);
            
            // 에러 발생 시 기본값 유지
            setTemperature(36.5);
        } finally {
            setLoading(false);
        }
    }, []);

    const handleRetry = useCallback(() => {
        if (retryCount < 3) { // 최대 3회까지 재시도
            setRetryCount(prev => prev + 1);
            loadUserProfile();
        }
    }, [retryCount, loadUserProfile]);

    useEffect(() => {
        loadUserProfile();
    }, [loadUserProfile]);

    if (loading) {
        return (
            <div className="mb-6">
                <div className="bg-white rounded-lg shadow-sm p-6 text-center">
                    <div className="flex items-center justify-center mb-2">
                        <i 
                            className="fas fa-spinner fa-spin text-orange-500 text-2xl mr-2" 
                            aria-hidden="true"
                            role="img"
                            aria-label="로딩 중"
                        ></i>
                        <div className="text-2xl font-bold text-gray-400" aria-live="polite">
                            --.-°C
                        </div>
                    </div>
                    <div className="text-gray-600 text-sm">온도 정보 로딩 중...</div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="mb-6">
                <div className="bg-white rounded-lg shadow-sm p-6 text-center">
                    <div className="flex items-center justify-center mb-2">
                        <i 
                            className="fas fa-thermometer-half text-orange-500 text-2xl mr-2" 
                            aria-hidden="true"
                            role="img"
                            aria-label="온도계"
                        ></i>
                        <div className="text-2xl font-bold text-orange-600">
                            {temperature.toFixed(1)}°C
                        </div>
                    </div>
                    <div className="text-gray-600 text-sm">사용자 온도 (기본값)</div>
                    <div className="text-red-500 text-xs mt-2" role="alert">
                        {error}
                    </div>
                    {retryCount < 3 && (
                        <button
                            onClick={handleRetry}
                            className="mt-2 px-3 py-1 text-xs bg-orange-500 text-white rounded hover:bg-orange-600 focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2 transition-colors"
                            aria-label="온도 정보 다시 불러오기"
                        >
                            다시 시도 ({retryCount}/3)
                        </button>
                    )}
                </div>
            </div>
        );
    }

    return (
        <div className="mb-6" role="region" aria-label="사용자 온도 정보">
            <TemperatureCard temperature={temperature} />
        </div>
    );
};

export default ProfileStats;