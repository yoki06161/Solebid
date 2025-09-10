import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router-dom';

import AuctionPage from './features/auction/pages/AuctionPage';
import CartPage from './features/cart/pages/CartPage';
import CategoryPage from './features/category/pages/CategoryPage';
import MainPage from './features/main/pages/MainPage';
import NotificationPage from './features/nofitication/pages/NotificationPage';
import OrderDetailPage from './features/order/pages/OrderDetailPage';
import OrderPage from "./features/order/pages/OrderPage";
import PolicyPage from './features/policy/pages/PolicyPage';
import ProfilePage from './features/profile/pages/ProfilePage';
import SearchPage from './features/search/pages/SearchPage';
import SettingPage from './features/setting/pages/SettingPage';
import TransactionDetailPage from './features/transaction/pages/TransactionDetailPage';
import TransactionPage from './features/transaction/pages/TransactionPage';
import WishPage from './features/wish/pages/WishPage';

import Login from "./features/user/pages/Login.tsx";
import NicknameSetup from "./features/user/pages/NicknameSetup.tsx";
import OAuth2Callback from "./features/user/pages/OAuth2Callback.tsx";
import Signup from "./features/user/pages/Signup.tsx";
import FindPassword from './features/user/pages/FindPassword';
import ResetPassword from './features/user/pages/ResetPassword';

import AppLayout from "./components/AppLayout.tsx";
import ProtectedRoute from './features/user/components/ProtectedRoute';

import ChargePointsPage from './features/payment/pages/ChargePointsPage.tsx';
import ChargeResultPage from './features/payment/pages/ChargeResultPage.tsx';
import PaymentRecordsPage from './features/payment/pages/PaymentRecordsPage.tsx';
import NewAuctionProductPage from "./features/product/pages/NewAuctionProductPage.tsx";

const router = createBrowserRouter(
    createRoutesFromElements(
        <Route element={<AppLayout />}>
            <Route path="/" element={<MainPage />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/login" element={<Login />} />
            <Route path="/auth/callback/:provider" element={<OAuth2Callback />} />
            <Route path="/nickname-setup" element={<NicknameSetup />} />
            <Route path="/auction" element={<AuctionPage />} />
            <Route path="/category/:categoryName" element={<CategoryPage />} />
            <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
            <Route path="/order" element={<ProtectedRoute><OrderPage /></ProtectedRoute>} />
            <Route path="/order/:orderId" element={<ProtectedRoute><OrderDetailPage /></ProtectedRoute>} />
            <Route path="/wish" element={<ProtectedRoute><WishPage /></ProtectedRoute>} />
            <Route path="/points/charge" element={<ProtectedRoute><ChargePointsPage /></ProtectedRoute>} />
            <Route path="/points/records" element={<ProtectedRoute><PaymentRecordsPage /></ProtectedRoute>} />
            <Route path="/products/new" element={<ProtectedRoute><NewAuctionProductPage /></ProtectedRoute>} />
            <Route path="/result" element={<ChargeResultPage />} />
            <Route path="/cart" element={<CartPage />} />
            <Route path="/policy" element={<PolicyPage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/notification" element={<ProtectedRoute><NotificationPage /></ProtectedRoute>} />
            <Route path="/setting" element={<ProtectedRoute><SettingPage /></ProtectedRoute>} />
            <Route path="/transaction" element={<ProtectedRoute><TransactionPage /></ProtectedRoute>} />
            <Route path="/transaction/:orderId" element={<ProtectedRoute><TransactionDetailPage /></ProtectedRoute>} />
            <Route path="/find-password" element={<FindPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
        </Route>
    )
)

export default router;
