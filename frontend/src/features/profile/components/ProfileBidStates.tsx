interface ProfileBidLoadingProps {
    title: string;
}

interface ProfileBidErrorProps {
    title: string;
    error: string;
}

interface ProfileBidEmptyProps {
    message?: string;
}

export const ProfileBidLoading = ({ title }: ProfileBidLoadingProps) => (
    <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
        <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
        </div>
        <div className="flex justify-center items-center py-8">
            <div className="text-gray-500">로딩 중...</div>
        </div>
    </div>
);

export const ProfileBidError = ({ title, error }: ProfileBidErrorProps) => (
    <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
        <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
        </div>
        <div className="flex justify-center items-center py-8">
            <div className="text-red-500">{error}</div>
        </div>
    </div>
);

export const ProfileBidEmpty = ({ message = "낙찰 내역이 없습니다." }: ProfileBidEmptyProps) => (
    <div className="text-center py-8 text-gray-500">
        {message}
    </div>
);