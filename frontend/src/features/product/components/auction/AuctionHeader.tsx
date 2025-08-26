const AuctionHeader = () => {
    return (
        <div className="max-w-[1440px] mx-auto px-6 h-16 flex items-center justify-between">
            <div className="flex items-center space-x-8">
                <h1 className="text-2xl font-bold text-blue-600">
                    SHOEBID
                </h1>
                <div className="hidden md:flex space-x-6">
                    <span className="text-blue-600 font-medium">
                        경매
                    </span>
                    <a
                        href="https://readdy.ai/home/8c14b666-4886-429c-ad07-c16c2cd22c03/9e3036ba-e6e2-46ed-842f-477bb9063a86"
                        data-readdy="true"
                        className="text-gray-600 hover:text-gray-900">
                        브랜드
                    </a>
                    <a
                        href="#"
                        className="text-gray-600 hover:text-gray-900">
                        랭킹
                    </a>
                </div>
            </div>
            <div className="flex items-center space-x-4">
                <div className="relative">
                    <input
                        type="search"
                        placeholder="상품 검색"
                        className="w-64 pl-10 pr-4 py-2 bg-gray-100 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <i className="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 text-sm" />
                </div>
                <button
                    onClick={() => { }}
                    className="px-4 py-2 bg-blue-500 text-white !rounded-button hover:bg-blue-600 whitespace-nowrap cursor-pointer"
                >
                    경매 등록
                </button>
                <button
                    onClick={() => { }}
                    className="px-4 py-2 border border-gray-300 text-gray-700 !rounded-button hover:bg-gray-50 whitespace-nowrap cursor-pointer"
                >
                    로그인
                </button>
                <button
                    onClick={() => { }}
                    className="px-4 py-2 bg-gray-900 text-white !rounded-button hover:bg-gray-800 whitespace-nowrap cursor-pointer">
                    회원가입
                </button>
            </div>
        </div>
    );
};

export default AuctionHeader;