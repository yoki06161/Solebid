import React, { useState } from "react";
import { BrandList, BrandModal, BrandPopular } from "../components/brand";
import { brandProducts, popularBrands } from "../components/brand/mockData";
import type { BrandProduct } from "../types/brand/Brand";

const BrandPage = () => {
    const [showModal, setShowModal] = React.useState<boolean>(false);
    const [selectedProduct, setSelectedProduct] = useState<BrandProduct | null>(null);

    const handleBidClick = (product: BrandProduct) => {
        setSelectedProduct(product);
        setShowModal(true);
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setSelectedProduct(null);
    }

    const handleBidSubmit = (productId: number, amount: number) => {
        console.log(`Submitting bid for product ${productId} with amount ${amount}`)
        alert("입찰이 완료되었습니다.");
        setShowModal(false);
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <main className="max-w-[1440px] mx-auto px-6 pt-6 pb-12">
                <BrandPopular brands={popularBrands} />
                <section>
                    <h2 className="text-2xl font-bold mb-8">브랜드별 상품</h2>
                    {brandProducts.map((brandData) => (
                        <BrandList
                            key={brandData.brand}
                            brandData={brandData}
                            onBidClick={handleBidClick}
                        />
                    ))}
                </section>
            </main>
            <BrandModal
                isOpen={showModal}
                onClose={handleCloseModal}
                product={selectedProduct}
                onSubmit={handleBidSubmit}
            />
        </div>
    );
};

export default BrandPage;