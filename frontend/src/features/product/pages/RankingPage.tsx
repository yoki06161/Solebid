import { useState } from "react";
import { RankingBidderList, RankingProductList, RankingSellerList, RankingTab } from "../components/ranking";
import { bidderRankings, productRankings, sellerRankings } from "../components/ranking/mockData";
import type { Tab } from "../types/ranking/RankingTabProps";

const RankingPage = () => {
    const [activeTab, setActiveTab] = useState<Tab>("products");
    const renderContent = () => {
        switch (activeTab) {
            case "products":
                return <RankingProductList items={productRankings} />;
            case "sellers":
                return <RankingSellerList items={sellerRankings} />;
            case "bidders":
                return <RankingBidderList items={bidderRankings} />;
            default:
                return null;
        }
    };
    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-[1440px] mx-auto px-6 py-8">
                <RankingTab activeTab={activeTab} onTabClick={setActiveTab} />
                {renderContent()}
            </div>
        </div>
    );
};

export default RankingPage;
