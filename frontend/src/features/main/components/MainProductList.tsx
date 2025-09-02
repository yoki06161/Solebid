import { useState } from "react";
import type { MainProductListProps } from "../types/MainProductListProps";
import MainProduct from "./MainProduct";

const MainProductList = ({ products }: MainProductListProps) => {
    const [currentTab, setCurrentTab] = useState<string>("trending");
    return (
        <div className="max-w-[1440px] mx-auto px-6 pb-12">
            <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-semibold text-gray-900">
                    실시간 인기 경매
                </h3>
                <div className="flex space-x-4">
                    <button
                        onClick={() => setCurrentTab("trending")}
                        className={
                            `px-4 py-2 rounded-lg cursor-pointer whitespace-nowrap shadow-sm
                             ${currentTab === "trending"
                                ? "bg-blue-500 text-white"
                                : "bg-white text-gray-600"
                            }`
                        }
                    >
                        인기순
                    </button>
                    <button
                        onClick={() => setCurrentTab("ending")}
                        className={
                            `px-4 py-2 rounded-lg cursor-pointer whitespace-nowrap shadow-sm 
                            ${currentTab === "ending"
                                ? "bg-blue-500 text-white"
                                : "bg-white text-gray-600"
                            }`
                        }
                    >
                        마감임박
                    </button>
                </div>
            </div>
            <div className="grid grid-cols-4 gap-6">
                {products.map((product) => (
                    <MainProduct
                        key={product.id}
                        product={product}
                    />
                ))}
            </div>
        </div>
    );
};

export default MainProductList;