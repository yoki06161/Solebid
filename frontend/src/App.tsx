import { RouterProvider } from "react-router-dom";
import router from "./router";
import TokenRefreshManager from "./components/TokenRefreshManager";

function App() {
    return (
        <>
            <TokenRefreshManager />
            <RouterProvider router={router} />
        </>
    );
}

export default App;
