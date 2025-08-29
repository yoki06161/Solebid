import React, { useState } from "react";
import { PAYMENT_OPTIONS } from "../constants/paymentOptions";
import type { PaymentId, RegisteredPayments } from "../types";

type Props = {
    selectedPayment: PaymentId | "";
    setSelectedPayment: (v: PaymentId) => void;
    registeredPayments: RegisteredPayments;
    setSelectedCard: (id: number | null) => void;
    setSelectedAccount: (id: number | null) => void;
};

const PaymentMethodList: React.FC<Props> = ({
                                                selectedPayment,
                                                setSelectedPayment,
                                                registeredPayments,
                                                setSelectedCard,
                                                setSelectedAccount,
                                            }) => {
    const [showCardDetails, setShowCardDetails] = useState(false);

    return (
        <div className="bg-white rounded-lg shadow-sm border border-gray-300 p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-6">결제 수단 선택</h2>

            <div className="space-y-3">
                {PAYMENT_OPTIONS.map((payment) => {
                    const inputId = `pay-${payment.id}`;
                    const selected = selectedPayment === payment.id;

                    return (
                        <div
                            key={payment.id}
                            className={`flex items-start p-4 border rounded-lg transition-colors cursor-pointer ${
                                selected ? "bg-blue-50 border-blue-200" : "border-gray-300 hover:bg-gray-50"
                            }`}
                            onClick={() => setSelectedPayment(payment.id as PaymentId)} // 카드 전체 클릭으로도 선택
                            role="radio"
                            aria-checked={selected}
                            tabIndex={0}
                            onKeyDown={(e) => {
                                if (e.key === "Enter" || e.key === " ") {
                                    e.preventDefault();
                                    setSelectedPayment(payment.id as PaymentId);
                                }
                            }}
                        >
                            {/* 실제 라디오 */}
                            <input
                                id={inputId}
                                type="radio"
                                name="payment"
                                value={payment.id}
                                checked={selected}
                                onChange={(e) => setSelectedPayment(e.target.value as PaymentId)}
                                className="w-4 h-4 mt-1 border-gray-300 focus:ring-2 text-blue-600 focus:ring-blue-500"
                                onClick={(e) => e.stopPropagation()}
                            />
                            <div className="ml-4 flex items-start space-x-4 flex-1">
                                <i className={`${payment.icon} text-xl text-gray-600`} />
                                <div className="relative w-full">
                                    {/* 클릭 가능 라벨 */}
                                    <label
                                        htmlFor={inputId}
                                        className="font-medium text-gray-900 cursor-pointer select-none"
                                        onClick={(e) => e.stopPropagation()}
                                    >
                                        {payment.name}
                                    </label>

                                    {/* Quick Card */}
                                    {payment.id === "quickCard" ? (
                                        <div className="text-sm text-gray-500">
                                            {registeredPayments.cards.length > 0 ? (
                                                <div className="mt-3 grid grid-cols-1 gap-3" onClick={(e) => e.stopPropagation()}>
                                                    {payment.details.cards.map((card) => (
                                                        <div
                                                            key={card.id}
                                                            className="w-full p-3 border rounded-lg transition-all duration-200 hover:border-blue-300 cursor-pointer"
                                                            onClick={() => {
                                                                setSelectedCard(card.id);
                                                                setSelectedPayment("quickCard");
                                                            }}
                                                        >
                                                            <div className="flex items-center justify-between">
                                                                <div>
                                                                    <div className="font-medium">{card.name}</div>
                                                                    <div className="text-xs text-gray-500">**** {card.last4}</div>
                                                                </div>
                                                                <i
                                                                    className={`${
                                                                        card.type === "VISA" ? "fab fa-cc-visa" : "fab fa-cc-mastercard"
                                                                    } text-xl`}
                                                                />
                                                            </div>
                                                        </div>
                                                    ))}
                                                    <a
                                                        href="#"
                                                        onClick={(e) => e.stopPropagation()}
                                                        className="inline-block text-center px-4 py-3 border border-dashed border-gray-300 rounded-lg hover:border-blue-300 hover:bg-blue-50 transition-all duration-200"
                                                    >
                                                        <i className="fas fa-plus mr-2" />
                                                        새 카드 추가하기
                                                    </a>
                                                </div>
                                            ) : (
                                                <a
                                                    href="#"
                                                    onClick={(e) => e.stopPropagation()}
                                                    className="inline-block mt-2 px-4 py-2 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-colors"
                                                >
                                                    <i className="fas fa-plus mr-2" />
                                                    카드 간편결제 연결하기
                                                </a>
                                            )}
                                        </div>
                                    ) : payment.id === "quickBank" ? (
                                        // Quick Bank
                                        <div className="text-sm text-gray-500">
                                            {registeredPayments.accounts.length > 0 ? (
                                                <div className="mt-3 grid grid-cols-1 gap-3" onClick={(e) => e.stopPropagation()}>
                                                    {payment.details.accounts.map((account) => (
                                                        <div
                                                            key={account.id}
                                                            className="w-full p-3 border rounded-lg transition-all duration-200 hover:border-blue-300 cursor-pointer"
                                                            onClick={() => {
                                                                setSelectedAccount(account.id);
                                                                setSelectedPayment("quickBank");
                                                            }}
                                                        >
                                                            <div className="flex items-center justify-between">
                                                                <div>
                                                                    <div className="font-medium">{account.bank}</div>
                                                                    <div className="text-xs text-gray-500">**** {account.last4}</div>
                                                                    <div className="text-xs text-gray-400">{account.accountType}</div>
                                                                </div>
                                                                <i className="fas fa-university text-xl text-gray-600" />
                                                            </div>
                                                        </div>
                                                    ))}
                                                    <a
                                                        href="#"
                                                        onClick={(e) => e.stopPropagation()}
                                                        className="inline-block text-center px-4 py-3 border border-dashed border-gray-300 rounded-lg hover:border-blue-300 hover:bg-blue-50 transition-all duration-200"
                                                    >
                                                        <i className="fas fa-plus mr-2" />
                                                        새 계좌 추가하기
                                                    </a>
                                                </div>
                                            ) : (
                                                <a
                                                    href="#"
                                                    onClick={(e) => e.stopPropagation()}
                                                    className="inline-block mt-2 px-4 py-2 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-colors"
                                                >
                                                    <i className="fas fa-plus mr-2" />
                                                    계좌 간편결제 연결하기
                                                </a>
                                            )}
                                        </div>
                                    ) : payment.id === "card" ? (
                                        <button
                                            type="button"
                                            className="text-sm text-gray-500 cursor-pointer hover:text-blue-600 flex items-center"
                                            onClick={(e) => {
                                                e.preventDefault();
                                                e.stopPropagation();
                                                setShowCardDetails((prev) => !prev);
                                            }}
                                        >
                                            {payment.desc}
                                            <i className={`fas fa-chevron-${showCardDetails ? "up" : "down"} ml-1 text-xs`} />
                                        </button>
                                    ) : (
                                        <div className="text-sm text-gray-500">{payment.desc}</div>
                                    )}

                                    {/* 카드 상세 패널 */}
                                    {payment.id === "card" && showCardDetails && "details" in payment && (
                                        <div className="absolute top-full left-0 mt-2 w-72 bg-white rounded-lg shadow-lg border border-gray-200 p-4 z-10">
                                            <div className="space-y-4">
                                                <div>
                                                    <h4 className="text-sm font-semibold text-gray-900 mb-2">지원 카드사</h4>
                                                    <div className="grid grid-cols-2 gap-2">
                                                        {payment.details.cards.map((card) => (
                                                            <div key={card.name} className="flex items-center space-x-2">
                                                                <i className={`${card.icon} text-lg`} />
                                                                <span className="text-sm text-gray-600">{card.name}</span>
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                                <div>
                                                    <h4 className="text-sm font-semibold text-gray-900 mb-1">수수료</h4>
                                                    <p className="text-sm text-gray-600">{payment.details.fees}</p>
                                                </div>
                                                <div>
                                                    <h4 className="text-sm font-semibold text-gray-900 mb-1">할부 정보</h4>
                                                    <p className="text-sm text-gray-600">{payment.details.installment}</p>
                                                </div>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default PaymentMethodList;
