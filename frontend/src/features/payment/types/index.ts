export type PaymentId = "card" | "quickCard" | "quickBank";

export interface RegisteredCard {
    id: number;
    name: string;
    last4: string;
    type: "VISA" | "Mastercard" | "American Express" | "JCB" | string;
}

export interface RegisteredAccount {
    id: number;
    bank: string;
    last4: string;
    accountType: string; // e.g., "입출금"
}

export interface RegisteredPayments {
    cards: RegisteredCard[];
    accounts: RegisteredAccount[];
}

export type CardBrand = "VISA" | "Mastercard" | "American Express" | "JCB";

export type GenericPayment = {
    id: "bank";
    name: string;
    icon: string;
    desc: string;
};

export type NormalCardPayment = {
    id: "card";
    name: string;
    icon: string;
    desc: string;
    details: {
        cards: { name: CardBrand; icon: string }[];
        fees: string;
        installment: string;
    };
};

export type QuickCardPayment = {
    id: "quickCard";
    name: string;
    icon: string;
    desc: string;
    details: {
        cards: RegisteredCard[];
    };
};

export type QuickBankPayment = {
    id: "quickBank";
    name: string;
    icon: string;
    desc: string;
    details: {
        accounts: RegisteredAccount[];
    };
};

export type PaymentOption =
    | GenericPayment
    | NormalCardPayment
    | QuickCardPayment
    | QuickBankPayment;
