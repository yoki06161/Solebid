import React from "react";
import type { PaymentId } from "../types";
import { formatNumber } from "../constants/presets";

type Props = {
    selectedAmount: number;
    selectedPayment: PaymentId | "";
    onSubmit?: () => void;
};

const SummaryCard: React.FC<Props> = ({
                                          selectedAmount,
                                          selectedPayment,
                                          onSubmit,
                                      }) => {
    return (
        <div className="bg-white rounded-lg shadow-sm border border-gray-300 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">결제 정보</h3>

            <div className="space-y-3 mb-6">
                <div className="flex justify-between">
                    <span className="text-gray-600">충전 금액</span>
                    <span className="font-medium">
            {selectedAmount > 0 ? `${formatNumber(selectedAmount)}원` : "-"}
          </span>
                </div>

                <div className="flex justify-between">
                    <span className="text-gray-600">전환 포인트</span>
                    <span className="font-medium text-blue-600">
            {selectedAmount > 0 ? `${formatNumber(selectedAmount)}P` : "-"}
          </span>
                </div>

                <div className="flex justify-between">
                    <span className="text-gray-600">결제 수단</span>
                    <span className="font-medium">
            {selectedPayment === "card"
                ? "일반결제"
                : selectedPayment === "quickCard"
                    ? "카드 간편결제"
                    : selectedPayment === "quickBank"
                        ? "계좌 간편결제"
                        : "-"}
          </span>
                </div>

                <div className="border-t border-gray-300 pt-3">
                    <div className="flex justify-between text-lg font-semibold">
                        <span>총 결제 금액</span>
                        <span className="text-blue-600">
              {selectedAmount > 0 ? `${formatNumber(selectedAmount)}원` : "-"}
            </span>
                    </div>
                </div>
            </div>

            <button
                className={`block text-center !rounded-button whitespace-nowrap cursor-pointer w-full py-4 rounded-lg font-semibold transition-all duration-200 ${
                    selectedAmount > 0
                        ? "bg-blue-600 hover:bg-blue-700 text-white"
                        : "bg-gray-300 text-gray-500 cursor-not-allowed"
                }`}
                onClick={(e) => {
                    if (selectedAmount <= 0) {
                        e.preventDefault();
                        return;
                    }
                    onSubmit?.();
                }}
            >
                <i className="fas fa-credit-card mr-2" />
                충전하기
            </button>

            <button
                type="button"
                className="!rounded-button whitespace-nowrap cursor-pointer w-full mt-3 py-3 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
            >
                취소
            </button>
        </div>
    );
};

export default SummaryCard;
