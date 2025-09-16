import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

import InfoBanner from "../components/InfoBanner";
import AmountSelector from "../components/AmountSelector";
import PaymentMethodList from "../components/PaymentMethodList";
import SummaryCard from "../components/SummaryCard";
import SecurityInfo from "../components/SecurityInfo";
import AdditionalInfo from "../components/AdditionalInfo";

import { PRESET_AMOUNTS } from "../constants/presets";
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

    // UI → PortOne pay_method 매핑
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
                        <div className="flex items-center space-x-2"></div>
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
                            setSelectedPayment={setSelectedPayment}
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
                                    // 테스트용 100원 허용
                                    const isTestAmount = selectedAmount === 100;

                                    if ((!isTestAmount && selectedAmount < 1000) || selectedAmount > 1_000_000) {
                                        alert("충전 금액은 1,000원 ~ 1,000,000원 사이여야 합니다. (테스트용 100원 허용)");
                                        return;
                                    }

                                    if (!selectedPayment) {
                                        alert("결제 수단을 선택해 주세요.");
                                        return;
                                    }

                                    // PortOne 결제창 바로 호출 (redirectUrl 사용 안 함 → INICIS 페이지로 안 튐)
                                    const result = await startPortoneCharge({
                                        amount: selectedAmount,
                                        payMethod: toPayMethod(selectedPayment),
                                        // redirectUrl: 사용하지 않음
                                        buyer: {
                                            // email/name/tel 필요 시 주입
                                        },
                                    });

                                    // startPortoneCharge가 camelCase로 가공해 준다고 가정 (impUid/merchantUid/orderId)
                                    if (!result.impUid) {
                                        alert("결제가 취소되었거나 실패했습니다.");
                                        return;
                                    }

                                    // 서버 검증/포인트 적립이 있다면 여기서 처리
                                    // await finalizePortoneCharge({
                                    //   impUid: result.impUid,
                                    //   merchantUid: result.merchantUid,
                                    //   amount: selectedAmount,
                                    // });

                                    // 프로필로 이동 + 안내 배너용 state 전달
                                    navigate("/profile", {
                                        state: { charged: true, chargedAmount: selectedAmount },
                                        replace: true,
                                    });
                                } catch (e) {
                                    console.error(e);
                                    alert("결제에 실패했습니다. 다시 시도해 주세요.");
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
