import type { ReactNode } from "react";

export interface ProfileBidSectionProps {
    title: string;
    linkTo?: string;
    linkText?: string;
    children: ReactNode;
}
