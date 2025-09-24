// src/context/toast/ToastContext.tsx
import React, { createContext, useContext, useMemo } from "react";
import { toast, ToastContainer } from "react-toastify";
import type { ToastOptions } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

export type ToastContextType = {
    success: (msg: string, opts?: ToastOptions) => void;
    error: (msg: string, opts?: ToastOptions) => void;
    warning: (msg: string, opts?: ToastOptions) => void;
    info: (msg: string, opts?: ToastOptions) => void;
    /** 간단히 메시지 하나만 띄우고 싶을 때 */
    showToast: (msg: string, opts?: ToastOptions) => void;
};

const ToastContext = createContext<ToastContextType | null>(null);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const api: ToastContextType = useMemo(
        () => ({
            success: (msg, opts) => toast.success(msg, opts),
            error: (msg, opts) => toast.error(msg, opts),
            warning: (msg, opts) => toast.warn(msg, opts),
            info: (msg, opts) => toast.info(msg, opts),
            showToast: (msg, opts) => toast(msg, opts),
        }),
        []
    );

    return (
        <ToastContext.Provider value={api}>
            {children}
            {/* 전역 토스트 컨테이너 */}
            <ToastContainer
                position="top-right"
                autoClose={3000}
                hideProgressBar={false}
                newestOnTop
                closeOnClick
                pauseOnHover
                draggable
            />
        </ToastContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export function useToast() {
    const ctx = useContext(ToastContext);
    if (!ctx) throw new Error("useToast must be used within ToastProvider");
    return ctx;
}
