import { useState, type ReactNode, useCallback } from "react";
import Toast from "../../components/Toast";
import { ToastContext } from "./toast";

export const ToastProvider = ({ children }: { children: ReactNode }) => {
    const [toastMessage, setToastMessage] = useState<string | null>(null);

    const showToast = useCallback((message: string) => {
        setToastMessage(message);
        setTimeout(() => {
            setToastMessage(null);
        }, 3000);
    }, []);

    return (
        <ToastContext.Provider
            value={{ showToast }}>
            {children}
            {toastMessage && (
                <Toast message={toastMessage} />
            )}
        </ToastContext.Provider>
    );
};
