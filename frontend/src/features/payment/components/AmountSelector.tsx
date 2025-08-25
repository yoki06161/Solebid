import React from "react";
import { formatNumber } from "../constants/presets";

type Props = {
    presets: number[];
    selectedAmount: number;
    showCustomInput: boolean;
    customAmount: string;
    onSelectAmount: (amount: number) => void;
    onShowCustom: () => void;
    onCustomChange: (v: string) => void;
};

const AmountSelector: React.FC<Props> = ({
                                             presets,
                                             selectedAmount,
                                             showCustomInput,
                                             customAmount,
                                             onSelectAmount,
                                             onShowCustom,
                                             onCustomChange,
                                         }) => {
    return (
        <div className="bg-white rounded-lg shadow-sm border border-gray-300 p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-6">충전 금액 선택</h2>

            <div className="grid grid-cols-2 gap-4 mb-6">
                {presets.map((amount) => (
                    <button
                        key={amount}
                        onClick={() => onSelectAmount(amount)}
                        className={`!rounded-button whitespace-nowrap cursor-pointer p-4 border-2 rounded-lg transition-all duration-200 ${
                            selectedAmount === amount && !showCustomInput
                                ? "border-blue-500 bg-blue-50 text-blue-700"
                                : "border-gray-300 hover:border-gray-300 text-gray-700"
                        }`}
                    >
                        <div className="text-lg font-semibold">{formatNumber(amount)}원</div>
                        <div className="text-sm text-gray-500">
                            {formatNumber(amount)}P 충전
                        </div>
                    </button>
                ))}
            </div>

            <div className="border-t border-gray-300 pt-6">
                <button
                    onClick={onShowCustom}
                    className={`!rounded-button whitespace-nowrap cursor-pointer w-full p-4 border-2 rounded-lg transition-all duration-200 ${
                        showCustomInput ? "border-blue-500 bg-blue-50" : "border-gray-300 hover:border-gray-300"
                    }`}
                >
                    <i className="fas fa-edit mr-2" />
                    직접 입력하기
                </button>

                {showCustomInput && (
                    <div className="mt-4">
                        <div className="relative">
                            <input
                                type="text"
                                value={customAmount}
                                onChange={(e) => onCustomChange(e.target.value)}
                                placeholder="충전할 금액을 입력하세요"
                                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-lg"
                                inputMode="numeric"
                                aria-label="충전 금액 입력"
                            />
                            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500">원</span>
                        </div>
                        <p className="text-sm text-gray-500 mt-2">최소 1,000원부터 충전 가능합니다</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AmountSelector;
