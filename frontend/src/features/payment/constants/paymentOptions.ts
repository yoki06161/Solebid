import type { PaymentOption } from "../types";

export const PAYMENT_OPTIONS: PaymentOption[] = [
    {
        id: "card",
        name: "일반 결제",
        icon: "fas fa-credit-card",
        desc: "모든 신용카드 사용 가능",
        details: {
            cards: [
                { name: "VISA", icon: "fab fa-cc-visa" },
                { name: "Mastercard", icon: "fab fa-cc-mastercard" },
                { name: "American Express", icon: "fab fa-cc-amex" },
                { name: "JCB", icon: "fab fa-cc-jcb" },
            ],
            fees: "수수료 무료",
            installment: "2-12개월 할부 가능",
        },
    },
    {
        id: "quickCard",
        name: "카드 간편결제",
        icon: "fas fa-credit-card",
        desc: "등록된 카드로 빠른 결제",
        details: {
            cards: [
                { id: 1, name: "신한카드", last4: "1234", type: "VISA" },
                { id: 2, name: "국민카드", last4: "5678", type: "Mastercard" },
            ],
        },
    },
    {
        id: "quickBank",
        name: "계좌 간편결제",
        icon: "fas fa-university",
        desc: "등록된 계좌로 빠른 결제",
        details: {
            accounts: [
                { id: 1, bank: "신한은행", last4: "1234", accountType: "입출금" },
                { id: 2, bank: "국민은행", last4: "5678", accountType: "입출금" },
            ],
        },
    },
];
