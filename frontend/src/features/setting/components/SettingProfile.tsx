const SettingProfile = () => {
    return (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <div className="text-center mb-6">
                <div className="relative inline-block">
                    <img
                        src="https://readdy.ai/api/search-image?query=professional%20korean%20business%20person%20portrait%20with%20clean%20white%20background%20modern%20lighting%20warm%20smile%20confident%20expression%20high%20quality%20photography&width=120&height=120&seq=profile001&orientation=squarish"
                        alt="프로필 이미지"
                        className="w-20 h-20 rounded-full object-cover mx-auto"
                    />
                    <button
                        onClick={() => { }}
                        className="absolute bottom-0 right-0 bg-blue-500 text-white rounded-full w-6 h-6 flex items-center justify-center cursor-pointer hover:bg-blue-600">
                        <i className="fas fa-camera text-xs" />
                    </button>
                </div>
                <h2 className="text-xl font-semibold text-gray-900 mt-3">
                    김민수
                </h2>
                <p className="text-gray-600 text-sm">
                    minsu.kim@email.com
                </p>
                <button
                    onClick={() => { }}
                    className="mt-3 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm hover:bg-gray-200 cursor-pointer !rounded-button whitespace-nowrap">
                    프로필 편집
                </button>
            </div>
        </div>
    );
};

export default SettingProfile;