import React from "react";
import ImageGridUploader from "../components/ImageGridUploader";
import { useProductSearch } from "../hooks/useProductSearch";
import type { Product } from "../types/product";

const CATEGORY = ["sneakers", "running", "basketball"] as const;
const BRANDS = ["nike", "adidas", "newbalance"] as const;

const NewAuctionProductPage: React.FC = () => {
    const { term, setTerm, results, open, setOpen } = useProductSearch(300);

    const onPickSearch = (p: Product) => {
        const productNameInput = document.getElementById(
            "productName"
        ) as HTMLInputElement | null;
        const brandSelect = document.getElementById(
            "brand"
        ) as HTMLSelectElement | null;
        const categorySelect = document.getElementById(
            "category"
        ) as HTMLSelectElement | null;
        const startPriceInput = document.getElementById(
            "startPrice"
        ) as HTMLInputElement | null;
        const modelCodeInput = document.getElementById(
            "modelCode"
        ) as HTMLInputElement | null;
        const colorwayInput = document.getElementById(
            "colorway"
        ) as HTMLSelectElement | HTMLInputElement | null;
        const releaseDateInput = document.getElementById(
            "releaseDate"
        ) as HTMLInputElement | null;

        if (productNameInput) productNameInput.value = p.name;
        if (brandSelect) brandSelect.value = p.brand;
        if (categorySelect) categorySelect.value = p.category;
        if (startPriceInput) startPriceInput.value = p.price.toString();
        if (modelCodeInput && p.modelCode) modelCodeInput.value = p.modelCode;
        if (colorwayInput && "value" in colorwayInput && p.colorway)
            colorwayInput.value = p.colorway;
        if (releaseDateInput && p.releaseDate)
            releaseDateInput.value = p.releaseDate;

        setOpen(false);
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Main */}
            <div className="max-w-[1440px] mx-auto px-6 py-8">
                <div className="bg-white rounded-lg shadow-sm p-8">
                    <h2 className="text-2xl font-bold text-gray-900 mb-8">
                        경매 상품 등록
                    </h2>

                    <form className="space-y-8" onSubmit={(e) => e.preventDefault()}>
                        {/* Image Upload */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-4">
                                상품 이미지
                            </label>
                            <ImageGridUploader />
                        </div>

                        {/* Basic Info */}
                        <div className="grid grid-cols-2 gap-6">
                            <div>
                                <label
                                    htmlFor="productName"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    상품명
                                </label>
                                <div className="relative">
                                    <input
                                        type="text"
                                        id="productName"
                                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        placeholder="상품명을 입력하세요"
                                        value={term}
                                        onChange={(e) => setTerm(e.target.value)}
                                    />
                                    {open && (
                                        <div className="absolute z-50 w-full mt-2 bg-white rounded-lg shadow-lg border border-gray-200 max-h-96 overflow-y-auto">
                                            {results.map((product) => (
                                                <div
                                                    key={product.id}
                                                    className="flex items-center space-x-4 p-4 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-b-0"
                                                    onClick={() => onPickSearch(product)}
                                                >
                                                    <img
                                                        src={product.image}
                                                        alt={product.name}
                                                        className="w-16 h-16 object-cover rounded-md"
                                                    />
                                                    <div>
                                                        <h3 className="font-medium text-gray-900">
                                                            {product.name}
                                                        </h3>
                                                        <p className="text-sm text-gray-500">
                                                            {product.brand} · {product.category}
                                                        </p>
                                                        <p className="text-sm font-medium text-gray-900">
                                                            ₩ {product.price.toLocaleString()}
                                                        </p>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </div>

                            <div>
                                <label
                                    htmlFor="brand"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    브랜드
                                </label>
                                <div className="relative">
                                    <select
                                        id="brand"
                                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 appearance-none cursor-pointer"
                                    >
                                        <option value="">브랜드 선택</option>
                                        {BRANDS.map((b) => (
                                            <option key={b} value={b}>
                                                {b}
                                            </option>
                                        ))}
                                    </select>
                                    <i className="fas fa-chevron-down absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
                                </div>
                            </div>

                            <div>
                                <label
                                    htmlFor="category"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    카테고리
                                </label>
                                <div className="relative">
                                    <select
                                        id="category"
                                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 appearance-none cursor-pointer"
                                    >
                                        <option value="">카테고리 선택</option>
                                        {CATEGORY.map((c) => (
                                            <option key={c} value={c}>
                                                {c}
                                            </option>
                                        ))}
                                    </select>
                                    <i className="fas fa-chevron-down absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
                                </div>
                            </div>

                            <div>
                                <label
                                    htmlFor="size"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    사이즈
                                </label>
                                <input
                                    type="text"
                                    id="size"
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    placeholder="사이즈를 입력하세요"
                                />
                            </div>

                            <div>
                                <label
                                    htmlFor="modelCode"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    모델 코드 <span className="text-gray-400 text-xs">(권장)</span>
                                </label>
                                <input
                                    type="text"
                                    id="modelCode"
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    placeholder="예) DZ5485-410"
                                    pattern="^[A-Za-z0-9-]+$"
                                    maxLength={60}
                                />
                            </div>

                            <div>
                                <label
                                    htmlFor="colorway"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    색상
                                </label>
                                <div className="relative">
                                    <select
                                        id="colorway"
                                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 appearance-none cursor-pointer"
                                    >
                                        <option value="">색상 선택</option>
                                        {[
                                            "black",
                                            "white",
                                            "gray",
                                            "red",
                                            "blue",
                                            "green",
                                            "yellow",
                                            "pink",
                                            "purple",
                                            "brown",
                                            "beige",
                                            "navy",
                                            "multi",
                                        ].map((c) => (
                                            <option key={c} value={c}>
                                                {c}
                                            </option>
                                        ))}
                                    </select>
                                    <i className="fas fa-chevron-down absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
                                </div>
                            </div>

                            <div>
                                <label
                                    htmlFor="releaseDate"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    출시일
                                </label>
                                <input
                                    type="date"
                                    id="releaseDate"
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    max={new Date().toISOString().split("T")[0]}
                                    min="1980-01-01"
                                />
                            </div>
                        </div>

                        {/* Auction Settings */}
                        <div className="grid grid-cols-2 gap-6">
                            <div>
                                <label
                                    htmlFor="startPrice"
                                    className="block text-sm font-medium text-gray-700 mb-1"
                                >
                                    시작가
                                </label>
                                <div className="relative">
                                    <input
                                        type="number"
                                        id="startPrice"
                                        className="w-full pl-8 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                                        placeholder="시작가를 입력하세요"
                                    />
                                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">
                    ₩
                  </span>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    경매 종료일
                                </label>
                                <div className="relative">
                                    <input
                                        type="datetime-local"
                                        id="auctionEndDate"
                                        className="w-full pl-4 pr-10 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 cursor-pointer"
                                        min={new Date(Date.now() + 24 * 60 * 60 * 1000)
                                            .toISOString()
                                            .slice(0, 16)}
                                        max={new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
                                            .toISOString()
                                            .slice(0, 16)}
                                        step={600}
                                        onClick={(e) => {
                                            const input = e.currentTarget as HTMLInputElement;
                                            try {
                                                (input as any).showPicker?.();
                                            } catch {
                                                // fallback 모달 필요 시 추가
                                            }
                                        }}
                                    />
                                    <i className="fas fa-clock absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none"></i>
                                </div>
                            </div>
                        </div>

                        {/* Product Description */}
                        <div>
                            <label
                                htmlFor="description"
                                className="block text-sm font-medium text-gray-700 mb-1"
                            >
                                상세 설명
                            </label>
                            <textarea
                                id="description"
                                rows={6}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                placeholder="상품에 대한 상세한 설명을 입력하세요"
                            />
                        </div>

                        {/* Actions */}
                        <div className="flex justify-end space-x-4">
                            <button
                                type="button"
                                className="px-6 py-3 border border-gray-300 text-gray-700 !rounded-button hover:bg-gray-50 whitespace-nowrap"
                            >
                                취소
                            </button>
                            <button
                                type="submit"
                                className="px-6 py-3 bg-blue-500 text-white !rounded-button hover:bg-blue-600 whitespace-nowrap"
                            >
                                등록하기
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default NewAuctionProductPage;
