import { RouterProvider } from "react-router-dom";
import TokenRefreshManager from "./components/TokenRefreshManager";
import { ModalProvider } from "./contexts/modal/ModalContext";
import { ToastProvider } from "./contexts/toast/ToastContext";
import router from "./router";

function App() {
    return (
        <>
            <ToastProvider>
                <ModalProvider>
                    <TokenRefreshManager />
                    <RouterProvider router={router} />
                </ModalProvider>
            </ToastProvider>
        </>
    );
}

export default App;
