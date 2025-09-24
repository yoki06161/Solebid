import { RouterProvider } from "react-router-dom";
import TokenRefreshManager from "./components/TokenRefreshManager";
import { ModalProvider } from "./context/modal/ModalContext";
import { ToastProvider } from "./context/toast/ToastContext";
import NotificationStreamProvider from "./context/notification/NotificationStreamProvider.tsx";
import router from "./router";

function App() {
    return (
        <>
            <ToastProvider>
                <ModalProvider>
                    <NotificationStreamProvider>
                    <TokenRefreshManager />
                    <RouterProvider router={router} />
                    </NotificationStreamProvider>
                </ModalProvider>
            </ToastProvider>
        </>
    );
}

export default App;
