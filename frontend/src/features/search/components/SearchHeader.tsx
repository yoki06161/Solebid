import type { SearchHeaderProps } from '../types/SearchHeaderProps';

const SearchHeader = ({
  searchQuery,
  setSearchQuery,
  handleClearSearch,
  handleCloseModal,
  handleSearch,
}: SearchHeaderProps) => {
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div className="sticky top-0 bg-white px-4 py-4 z-10">
      <div className="flex items-center space-x-4">
        <div className="flex-1 relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <i className="fas fa-search text-gray-400 text-sm" />
          </div>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="브랜드명, 모델명, 모델번호 등"
            className="w-full pl-10 pr-10 py-3 border border-gray-300 rounded-lg focus:ring-black focus:border-transparent text-sm"
            autoFocus
          />
          {searchQuery && (
            <button
              onClick={handleClearSearch}
              className="absolute inset-y-0 right-0 pr-3 flex items-center cursor-pointer"
            >
              <i className="fas fa-times text-gray-400 text-sm hover:text-gray-600" />
            </button>
          )}
        </div>
        <button
          onClick={handleCloseModal}
          className="p-2 hover:bg-gray-100 rounded-lg cursor-pointer"
        >
          <i className="fas fa-times text-gray-600 text-lg" />
        </button>
      </div>
    </div>
  );
};

export default SearchHeader;
