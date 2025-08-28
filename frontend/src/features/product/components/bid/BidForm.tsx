import type { BidFormProps } from '../../types/bid/BidFormProps';

const BidForm = ({ bidInfo, errors, onInfoChange, brands, categories, sizes }: BidFormProps) => {
    const handlePriceChange = (field: 'startPrice' | 'confirmationPrice', value: string) => {
        const numericValue = value.replace(/[^0-9]/g, "");
        onInfoChange(field, numericValue);
    }
    return (
        <div className="space-y-6">
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    상품명
                </label>
                <input
                    type="text"
                    placeholder="상품명을 입력하세요"
                    value={bidInfo.name}
                    onChange={(e) => onInfoChange("name", e.target.value)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                />
                {
                    errors.name &&
                    <p className="text-red-500 text-sm mt-1">
                        {errors.name}
                    </p>
                }
            </div>
            <div className="grid grid-cols-2 gap-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        브랜드
                    </label>
                    <select
                        value={bidInfo.brand}
                        onChange={(e) => onInfoChange("brand", e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                    >
                        <option value="">브랜드 선택</option>
                        {brands.map((brand) => (
                            <option
                                key={brand}
                                value={brand}>
                                {brand}
                            </option>
                        ))}
                    </select>
                    {
                        errors.brand &&
                        <p className="text-red-500 text-sm mt-1">
                            {errors.brand}
                        </p>
                    }
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        카테고리
                    </label>
                    <select
                        value={bidInfo.category}
                        onChange={(e) => onInfoChange("category", e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                    >
                        <option value="">카테고리 선택</option>
                        {categories.map((cat) => (
                            <option
                                key={cat}
                                value={cat}>
                                {cat}
                            </option>
                        ))}
                    </select>
                    {
                        errors.category &&
                        <p className="text-red-500 text-sm mt-1">
                            {errors.category}
                        </p>
                    }
                </div>
            </div>
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    사이즈
                </label>
                <div className="grid grid-cols-6 gap-2">
                    {sizes.map((size) => (
                        <button
                            key={size}
                            onClick={() => onInfoChange("size", size.toString())}
                            className={
                                `px-3 py-2 text-sm border !rounded-button 
                                ${bidInfo.size === size.toString()
                                    ? "bg-blue-500 text-white border-blue-500"
                                    : "border-gray-300 text-gray-700"
                                }`}
                        >
                            {size}
                        </button>
                    ))}
                </div>
                {
                    errors.size &&
                    <p className="text-red-500 text-sm mt-1">
                        {errors.size}
                    </p>
                }
            </div>
            <div className="grid grid-cols-2 gap-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        시작가
                    </label>
                    <input
                        type="text"
                        placeholder="시작가 입력"
                        value={bidInfo.startPrice.toLocaleString()}
                        onChange={(e) => handlePriceChange("startPrice", e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                    />
                    {
                        errors.startPrice &&
                        <p className="text-red-500 text-sm mt-1">
                            {errors.startPrice}
                        </p>
                    }
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        즉시 구매가
                    </label>
                    <input
                        type="text"
                        placeholder="즉시 구매가 입력"
                        value={bidInfo.confirmationPrice.toLocaleString()}
                        onChange={(e) => handlePriceChange("confirmationPrice", e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                    />
                    {
                        errors.confirmationPrice &&
                        <p className="text-red-500 text-sm mt-1">
                            {errors.confirmationPrice}
                        </p>
                    }
                </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        경매 시작일
                    </label>
                    <input
                        type="date"
                        value={bidInfo.startDate}
                        onChange={(e) => onInfoChange("startDate", e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                    />
                    {
                        errors.startDate &&
                        <p className="text-red-500 text-sm mt-1">
                            {errors.startDate}
                        </p>
                    }
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        경매 종료일
                    </label>
                    <input
                        type="date"
                        value={bidInfo.endDate}
                        onChange={(e) => onInfoChange("endDate", e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                    />
                    {
                        errors.endDate &&
                        <p className="text-red-500 text-sm mt-1">
                            {errors.endDate}
                        </p>
                    }
                </div>
            </div>
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    상품 상태
                </label>
                <div className="flex space-x-4">
                    {["새상품", "중고상품"].map((cond) => (
                        <label
                            key={cond}
                            className="flex items-center cursor-pointer"
                        >
                            <input
                                type="radio"
                                name="condition"
                                value={cond}
                                checked={bidInfo.condition === cond}
                                onChange={(e) => onInfoChange("condition", e.target.value)}
                            />
                            <span className="ml-2 text-gray-700">
                                {cond}
                            </span>
                        </label>
                    ))}
                </div>
                {
                    errors.condition &&
                    <p className="text-red-500 text-sm mt-1">
                        {errors.condition}
                    </p>
                }
            </div>
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                    상세 설명
                </label>
                <textarea
                    placeholder="상품의 상세 정보와 특이사항을 입력해주세요"
                    value={bidInfo.description}
                    onChange={(e) => onInfoChange("description", e.target.value)}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg h-32"
                ></textarea>
            </div>
        </div>
    );
};

export default BidForm;