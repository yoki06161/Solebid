import { createBrowserRouter, createRoutesFromElements, Route } from 'react-router-dom';

import CartPage from './features/cart/pages/CartPage';
import MainPage from './features/main/pages/MainPage';
import OrderPage from "./features/order/pages/OrderPage";
import AuctionPage from './features/product/pages/AuctionPage';
import BidPage from './features/product/pages/BidPage';
import BrandPage from './features/product/pages/BrandPage';
import CategoryPage from './features/product/pages/CategoryPage';
import RankingPage from './features/product/pages/RankingPage';
import SearchPage from './features/product/pages/SearchPage';
import WishPage from './features/product/pages/WishPage';
import ProfilePage from './features/profile/pages/ProfilePage';

import Login from "./features/user/pages/Login.tsx";
import NicknameSetup from "./features/user/pages/NicknameSetup.tsx";
import OAuth2Callback from "./features/user/pages/OAuth2Callback.tsx";
import Signup from "./features/user/pages/Signup.tsx";

import ChargePointsPage from './features/payment/pages/ChargePointsPage.tsx';
import ChargeResultPage from './features/payment/pages/ChargeResultPage.tsx';


const router = createBrowserRouter(
    createRoutesFromElements(
        <Route>
            <Route path="/" element={<MainPage />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/login" element={<Login />} />
            <Route path="/auth/callback/:provider" element={<OAuth2Callback />} />
            <Route path="/nickname-setup" element={<NicknameSetup />} />
            <Route path="/auction" element={<AuctionPage />} />
            <Route path="/bid" element={<BidPage />} />
            <Route path="/category/:categoryName" element={<CategoryPage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/brand" element={<BrandPage />} />
            <Route path="/ranking" element={<RankingPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/order" element={<OrderPage />} />
            <Route path="/wish" element={<WishPage />} />
            <Route path="/points/charge" element={<ChargePointsPage />} />
            <Route path="/result" element={<ChargeResultPage />} />
            <Route path="/cart" element={<CartPage />} />
        </Route>
    )
)

export default router;
