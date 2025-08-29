import { RouterProvider } from "react-router-dom";
import TokenRefreshManager from "./components/TokenRefreshManager";
import { ModalProvider } from "./contexts/modal/ModalContext";
import router from "./router";

function App() {
    return (
        <>
            <ModalProvider>
                <TokenRefreshManager />
                <RouterProvider router={router} />
            </ModalProvider>
        </>
    );
}

export default App;
