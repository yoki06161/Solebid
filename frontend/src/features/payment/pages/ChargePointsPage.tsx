// src/features/payment/pages/ChargePointsPage.tsx
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

import InfoBanner from "../components/InfoBanner";
import AmountSelector from "../components/AmountSelector";
import PaymentMethodList from "../components/PaymentMethodList";
import SummaryCard from "../components/SummaryCard";
import SecurityInfo from "../components/SecurityInfo";
import AdditionalInfo from "../components/AdditionalInfo";

import { PRESET_AMOUNTS, CURRENT_POINTS, formatNumber } from "../constants/presets";
import type { PaymentId, RegisteredPayments } from "../types";
import { startPortoneCharge } from "../services/portoneService";

const ChargePointsPage: React.FC = () => {
    const navigate = useNavigate();

    // 금액/입력
    const [selectedAmount, setSelectedAmount] = useState<number>(0);
    const [customAmount, setCustomAmount] = useState<string>("");
    const [showCustomInput, setShowCustomInput] = useState<boolean>(false);

    // 결제 선택
    const [selectedPayment, setSelectedPayment] = useState<PaymentId | "">("");
    const [, setSelectedCard] = useState<number | null>(null);
    const [, setSelectedAccount] = useState<number | null>(null);

    // 등록된 간편 결제 (mock)
    const [registeredPayments] = useState<RegisteredPayments>({ cards: [], accounts: [] });

    const handleAmountSelect = (amount: number) => {
        setSelectedAmount(amount);
        setShowCustomInput(false);
        setCustomAmount("");
    };

    const handleCustomAmountChange = (value: string) => {
        const numValue = parseInt(value.replace(/[^0-9]/g, ""), 10);
        if (!Number.isNaN(numValue)) {
            setSelectedAmount(numValue);
            setCustomAmount(value);
        } else {
            setSelectedAmount(0);
            setCustomAmount("");
        }
    };

    // payMethod 매핑: quickBank → 'trans', 나머지 → 'card'
    const toPayMethod = (p: PaymentId | ""): "card" | "trans" => {
        if (p === "quickBank") return "trans";
        return "card";
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <div className="bg-white shadow-sm border-b border-gray-300">
                <div className="max-w-4xl mx-auto px-6 py-4">
                    <div className="flex items-center justify-between">
                        <h1 className="text-2xl font-bold text-gray-900">포인트 충전</h1>
                        <div className="flex items-center space-x-2">
                            <i className="fas fa-coins text-yellow-500" />
                            <span className="text-sm text-gray-600">현재 보유 포인트</span>
                            <span className="text-lg font-semibold text-blue-600">
                {formatNumber(CURRENT_POINTS)}P
              </span>
                        </div>
                    </div>
                </div>
            </div>

            {/* Body */}
            <div className="max-w-4xl mx-auto px-6 py-8">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Main */}
                    <div className="lg:col-span-2 space-y-8">
                        <InfoBanner />

                        <AmountSelector
                            presets={PRESET_AMOUNTS}
                            selectedAmount={selectedAmount}
                            showCustomInput={showCustomInput}
                            customAmount={customAmount}
                            onSelectAmount={handleAmountSelect}
                            onShowCustom={() => {
                                setShowCustomInput(true);
                                setSelectedAmount(0);
                            }}
                            onCustomChange={handleCustomAmountChange}
                        />

                        <PaymentMethodList
                            selectedPayment={selectedPayment}
                            setSelectedPayment={(v) => setSelectedPayment(v)}
                            registeredPayments={registeredPayments}
                            setSelectedCard={setSelectedCard}
                            setSelectedAccount={setSelectedAccount}
                        />
                    </div>

                    {/* Sidebar */}
                    <div className="space-y-6 sticky top-6">
                        <SummaryCard
                            selectedAmount={selectedAmount}
                            selectedPayment={selectedPayment}
                            onSubmit={async () => {
                                try {
                                    if (selectedAmount <= 0) return;

                                    const result = await startPortoneCharge({
                                        amount: selectedAmount,
                                        payMethod: toPayMethod(selectedPayment),
                                        buyer: {
                                            // 필요 시 실제 사용자 정보로 대체
                                            // email: currentUser?.email,
                                            // name: currentUser?.name,
                                            // tel: currentUser?.phone,
                                        },
                                    });

                                    // 결제 성공 후 결과 페이지로 라우팅
                                    navigate(
                                        `/result?success=1` +
                                        `&imp_uid=${encodeURIComponent(result.impUid ?? "")}` +
                                        `&merchant_uid=${encodeURIComponent(result.merchantUid ?? "")}` +
                                        `&orderId=${encodeURIComponent(result.orderId)}`
                                    );
                                } catch {
                                    // 실패 시 결과 페이지로(간단 표시)
                                    navigate(`/result?success=0`);
                                }
                            }}
                        />
                        <SecurityInfo />
                    </div>
                </div>

                <AdditionalInfo />
            </div>
        </div>
    );
};

export default ChargePointsPage;
