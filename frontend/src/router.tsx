import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router-dom';

import CartPage from './features/cart/pages/CartPage';
import MainPage from './features/main/pages/MainPage';
import NotificationPage from './features/nofitication/pages/NotificationPage';
import OrderDetailPage from './features/order/pages/OrderDetailPage';
import OrderPage from "./features/order/pages/OrderPage";
import PolicyPage from './features/policy/pages/PolicyPage';
import AuctionPage from './features/product/pages/AuctionPage';
import BidPage from './features/product/pages/BidPage';
import CategoryPage from './features/product/pages/CategoryPage';
import ProfilePage from './features/profile/pages/ProfilePage';
import SearchPage from './features/search/pages/SearchPage';
import SettingPage from './features/setting/pages/SettingPage';
import WishPage from './features/wish/pages/WishPage';

import Login from "./features/user/pages/Login.tsx";
import NicknameSetup from "./features/user/pages/NicknameSetup.tsx";
import OAuth2Callback from "./features/user/pages/OAuth2Callback.tsx";
import Signup from "./features/user/pages/Signup.tsx";

import AppLayout from "./components/AppLayout.tsx";

import ChargePointsPage from './features/payment/pages/ChargePointsPage.tsx';
import ChargeResultPage from './features/payment/pages/ChargeResultPage.tsx';
import ProtectedRoute from './features/user/components/ProtectedRoute';

import PaymentRecordsPage from './features/payment/pages/PaymentRecordsPage.tsx';

const router = createBrowserRouter(
    createRoutesFromElements(
        <Route element={<AppLayout />}>
            <Route path="/" element={<MainPage />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/login" element={<Login />} />
            <Route path="/auth/callback/:provider" element={<OAuth2Callback />} />
            <Route path="/nickname-setup" element={<NicknameSetup />} />
            <Route path="/auction" element={<AuctionPage />} />
            <Route path="/bid" element={<BidPage />} />
            <Route path="/category/:categoryName" element={<CategoryPage />} />
            <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
            <Route path="/order" element={<ProtectedRoute><OrderPage /></ProtectedRoute>} />
            <Route path="/order/:orderId" element={<ProtectedRoute><OrderDetailPage /></ProtectedRoute>} />
            <Route path="/wish" element={<ProtectedRoute><WishPage /></ProtectedRoute>} />
            <Route path="/points/charge" element={<ProtectedRoute><ChargePointsPage /></ProtectedRoute>} />
            <Route path="/points/records" element={<ProtectedRoute><PaymentRecordsPage /></ProtectedRoute>} />
            <Route path="/result" element={<ChargeResultPage />} />
            <Route path="/cart" element={<CartPage />} />
            <Route path="/policy" element={<PolicyPage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/notification" element={<ProtectedRoute><NotificationPage /></ProtectedRoute>} />
            <Route path="/setting" element={<ProtectedRoute><SettingPage /></ProtectedRoute>} />
        </Route>
    )
)

export default router;
