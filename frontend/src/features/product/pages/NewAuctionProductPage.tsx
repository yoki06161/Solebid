// src/features/product/pages/NewAuctionProductPage.tsx
import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import { presign, uploadToS3 } from "../../upload/services/uploads";
import { makeSafeFileName } from "../../upload/utils/naming";
import { createProduct, finalizeImages } from "../services/products";
import { AuctionsApi } from "../../auction/services/auctions";

import type { Brand, Category, Condition, ProductCreatePayload } from "../types/product";

/** ---백엔드 Enum과 1:1 매칭--- */
const BRAND_OPTIONS: { value: Brand; label: string }[] = [
    { value: "NIKE", label: "Nike" },
    { value: "ADIDAS", label: "Adidas" },
    { value: "NB", label: "New Balance" },
    { value: "ASICS", label: "ASICS" },
    { value: "PUMA", label: "Puma" },
    { value: "REEBOK", label: "Reebok" },
    { value: "CONVERSE", label: "Converse" },
    { value: "VANS", label: "Vans" },
];

const CATEGORY_OPTIONS: { value: Category; label: string }[] = [
    { value: "SNEAKERS", label: "스니커즈" },
    { value: "RUNNING", label: "러닝화" },
    { value: "BASKETBALL", label: "농구화" },
    { value: "CANVAS", label: "캔버스" },
];

const BRAND_ALLOW: Brand[] = BRAND_OPTIONS.map((b) => b.value);
const CATEGORY_ALLOW: Category[] = CATEGORY_OPTIONS.map((c) => c.value);

const DEFAULT_STATUS = "AVAILABLE" as const;

/** ---- datetime-local 유틸(로컬 기준 & 10분 스냅) ---- */
const TEN_MIN_MS = 10 * 60 * 1000;
function toLocalDatetimeValue(d: Date) {
    const off = d.getTimezoneOffset() * 60000;
    const local = new Date(d.getTime() - off);
    return local.toISOString().slice(0, 16);
}
function roundUpTo10Min(d: Date) {
    return new Date(Math.ceil(d.getTime() / TEN_MIN_MS) * TEN_MIN_MS);
}
function roundNearest10Min(d: Date) {
    return new Date(Math.round(d.getTime() / TEN_MIN_MS) * TEN_MIN_MS);
}

const NewAuctionProductPage: React.FC = () => {
    const navigate = useNavigate();

    /** 이미지 상태 */
    const [selectedImages, setSelectedImages] = useState<File[]>([]);
    const [previewUrls, setPreviewUrls] = useState<string[]>([]);
    const [s3Keys, setS3Keys] = useState<string[]>([]);
    const isImageLimitReached = selectedImages.length >= 5;

    /** 폼 상태 */
    const [name, setName] = useState("");
    const [brand, setBrand] = useState<Brand>("NIKE");
    const [category, setCategory] = useState<Category>("SNEAKERS");
    const [condition, setCondition] = useState<Condition>("NEW");
    const [size, setSize] = useState<number | "">("");
    const [modelCode, setModelCode] = useState("");
    const [colorway, setColorway] = useState("");
    const [releaseDate, setReleaseDate] = useState(""); // YYYY-MM-DD
    const [description, setDescription] = useState("");

    /** 경매 표시용 (프론트 전용) */
    const [startPrice, setStartPrice] = useState<number | "">("");
    const [auctionEndAt, setAuctionEndAt] = useState("");

    const [submitting, setSubmitting] = useState(false);

    /** 종료일: 로컬 기준, 최소 +24h(10분 올림), 최대 +7d */
    const minEndAtDate = useMemo(
        () => roundUpTo10Min(new Date(Date.now() + 24 * 60 * 60 * 1000)),
        []
    );
    const maxEndAtDate = useMemo(
        () => new Date(minEndAtDate.getTime() + 7 * 24 * 60 * 60 * 1000),
        [minEndAtDate]
    );

    // 초기값 채우기
    useEffect(() => {
        if (!auctionEndAt) setAuctionEndAt(toLocalDatetimeValue(minEndAtDate));
    }, [auctionEndAt, minEndAtDate]);

    /** 이미지 업로드(JPG/PNG, 최대 5장) */
    const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const picked = Array.from(e.target.files || []);
        if (!picked.length) return;

        if (selectedImages.length + picked.length > 5) {
            alert("최대 5장까지만 업로드 가능합니다.");
            e.currentTarget.value = "";
            return;
        }
        const valid = picked.every((f) =>
            ["image/jpeg", "image/png", "image/jpg"].includes(f.type || "")
        );
        if (!valid) {
            alert("JPG/PNG만 업로드 가능합니다.");
            e.currentTarget.value = "";
            return;
        }

        try {
            const newFiles: File[] = [];
            const newPreviews: string[] = [];
            const newKeys: string[] = [];

            for (const f of picked) {
                const ct = f.type === "image/png" ? "image/png" : "image/jpeg";
                const safeName = makeSafeFileName(f.name, ct);
                const { key, putUrl } = await presign(safeName, ct); // 쿠키 인증
                await uploadToS3(putUrl, f, ct);
                newFiles.push(f);
                newPreviews.push(URL.createObjectURL(f));
                newKeys.push(key);
            }

            setSelectedImages((prev) => [...prev, ...newFiles]);
            setPreviewUrls((prev) => [...prev, ...newPreviews]);
            setS3Keys((prev) => [...prev, ...newKeys]);
        } catch (err) {
            alert(err instanceof Error ? err.message : "이미지 업로드 중 오류가 발생했습니다.");
        } finally {
            e.currentTarget.value = "";
        }
    };

    const removeImage = (index: number) => {
        if (previewUrls[index]) URL.revokeObjectURL(previewUrls[index]);
        setSelectedImages((prev) => prev.filter((_, i) => i !== index));
        setPreviewUrls((prev) => prev.filter((_, i) => i !== index));
        setS3Keys((prev) => prev.filter((_, i) => i !== index));
    };

    /** 제출 */
    const handleSubmit: React.FormEventHandler<HTMLFormElement> = async (e) => {
        e.preventDefault();
        if (submitting) return;

        if (!name.trim()) return alert("상품명을 입력하세요.");
        if (selectedImages.length === 0) return alert("이미지를 최소 1장 업로드해 주세요.");
        if (size === "" || Number(size) < 220 || Number(size) > 320) {
            return alert("사이즈는 220~320 사이여야 합니다.");
        }
        if (!BRAND_ALLOW.includes(brand)) {
            alert("브랜드를 다시 선택해 주세요.");
            return;
        }
        if (!CATEGORY_ALLOW.includes(category)) {
            alert("카테고리를 다시 선택해 주세요.");
            return;
        }

        setSubmitting(true);
        try {
            // 1) 이미지 페이로드
            const images: ProductCreatePayload["images"] = s3Keys.map((key, i) => ({
                filePath: key,
                fileName: selectedImages[i]?.name ?? `image-${i + 1}`,
                sortOrder: i,
                isThumbnail: i === 0,
            }));

            // 2) 상품 생성
            const payload: ProductCreatePayload = {
                category,
                status: DEFAULT_STATUS,
                condition,
                brand,
                size: Number(size),
                name,
                description,
                modelCode: modelCode || null,
                colorway: colorway || null,
                releaseDate: releaseDate || null,
                images,
            };

            const { productId } = await createProduct(payload); // 쿠키 인증

            // 3) 이미지 finalize
            await finalizeImages(productId); // 쿠키 인증

            // 4) 경매 자동 생성(시작가 & 종료일 있을 때만)
            try {
                if (startPrice !== "" && auctionEndAt) {
                    await AuctionsApi.create({
                        productId,
                        startPrice: Number(startPrice),
                        endAt: new Date(auctionEndAt).toISOString(),
                    }); // 쿠키 인증
                }
            } catch (e) {
                console.error(e);
                alert("상품은 등록되었지만, 경매 생성에 실패했습니다. 경매는 나중에 다시 시작해 주세요.");
            }

            // 5) 안내 & 리다이렉트
            alert("상품이 등록되었습니다. 경매가 시작됩니다.");
            navigate("/auction", { replace: true });

            // 정리
            previewUrls.forEach((u) => URL.revokeObjectURL(u));
            setSelectedImages([]);
            setPreviewUrls([]);
            setS3Keys([]);
            setName("");
            setBrand("NIKE");
            setCategory("SNEAKERS");
            setCondition("NEW");
            setSize("");
            setModelCode("");
            setColorway("");
            setReleaseDate("");
            setDescription("");
            setStartPrice("");
            setAuctionEndAt(toLocalDatetimeValue(minEndAtDate));
        } catch (err) {
            alert(err instanceof Error ? err.message : "등록 중 오류가 발생했습니다.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-[1440px] mx-auto px-6 py-8">
                <div className="bg-white rounded-lg shadow-sm p-8">
                    <h2 className="text-2xl font-bold text-gray-900 mb-8">경매 상품 등록</h2>

                    <form className="space-y-8" onSubmit={handleSubmit}>
                        {/* 이미지 업로드 */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-4">상품 이미지</label>
                            <div className="grid grid-cols-5 gap-4">
                                {Array.from({ length: 5 }).map((_, index) => (
                                    <div key={index} className="relative aspect-square">
                                        {index < previewUrls.length ? (
                                            <div className="relative h-full">
                                                <img
                                                    src={previewUrls[index]}
                                                    alt={`Preview ${index + 1}`}
                                                    className="w-full h-full object-cover rounded-lg"
                                                />
                                                <button
                                                    type="button"
                                                    onClick={() => removeImage(index)}
                                                    className="absolute top-2 right-2 w-8 h-8 bg-white rounded-full shadow-md flex items-center justify-center text-gray-600 hover:text-gray-900"
                                                    aria-label="이미지 삭제"
                                                >
                                                    <i className="fas fa-times" />
                                                </button>
                                            </div>
                                        ) : (
                                            <label
                                                className={`h-full flex flex-col items-center justify-center border-2 border-dashed rounded-lg cursor-pointer ${
                                                    isImageLimitReached
                                                        ? "border-gray-200 cursor-not-allowed opacity-50"
                                                        : "border-gray-300 hover:border-blue-500"
                                                }`}
                                            >
                                                <i className="fas fa-plus text-gray-400 mb-2" />
                                                <span className="text-sm text-gray-500">
                          {index === 0 ? "대표 이미지" : "추가 이미지"}
                        </span>
                                                <input
                                                    type="file"
                                                    accept="image/jpeg,image/png,image/jpg"
                                                    className="hidden"
                                                    disabled={isImageLimitReached}
                                                    onChange={handleImageUpload}
                                                    id={`image-upload-${index}`}
                                                />
                                            </label>
                                        )}
                                    </div>
                                ))}
                            </div>
                            <p className="mt-2 text-sm text-gray-500">최대 5장까지 업로드 가능 (JPG/PNG만 업로드 가능)</p>
                        </div>

                        {/* 기본 정보 */}
                        <div className="grid grid-cols-2 gap-6">
                            {/* 상품명 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">상품명</label>
                                <input
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    placeholder="상품명을 입력하세요"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                />
                            </div>

                            {/* 브랜드 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">브랜드</label>
                                <div className="relative">
                                    <select
                                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 appearance-none cursor-pointer"
                                        value={brand}
                                        onChange={(e) => setBrand(e.target.value as Brand)}
                                    >
                                        {BRAND_OPTIONS.map((b) => (
                                            <option key={b.value} value={b.value}>
                                                {b.label}
                                            </option>
                                        ))}
                                    </select>
                                    <i className="fas fa-chevron-down absolute right-4 top-1/2 -translate-y-1/2 text-gray-400" />
                                </div>
                            </div>

                            {/* 카테고리 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">카테고리</label>
                                <div className="relative">
                                    <select
                                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 appearance-none cursor-pointer"
                                        value={category}
                                        onChange={(e) => setCategory(e.target.value as Category)}
                                    >
                                        {CATEGORY_OPTIONS.map((c) => (
                                            <option key={c.value} value={c.value}>
                                                {c.label}
                                            </option>
                                        ))}
                                    </select>
                                    <i className="fas fa-chevron-down absolute right-4 top-1/2 -translate-y-1/2 text-gray-400" />
                                </div>
                            </div>

                            {/* 사이즈 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">사이즈</label>
                                <div className="relative">
                                    <input
                                        type="number"
                                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        placeholder="사이즈를 입력하세요"
                                        min={220}
                                        max={320}
                                        value={size}
                                        onChange={(e) => setSize(e.target.value ? Number(e.target.value) : "")}
                                    />
                                    <i className="fas fa-chevron-down absolute right-4 top-1/2 -translate-y-1/2 text-gray-300 pointer-events-none" />
                                </div>
                            </div>

                            {/* 모델 코드 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    모델 코드 <span className="text-gray-400 text-xs">(권장)</span>
                                </label>
                                <input
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    placeholder="예) DZ5485-410"
                                    pattern="^[A-Za-z0-9-]+$"
                                    maxLength={60}
                                    value={modelCode}
                                    onChange={(e) => setModelCode(e.target.value)}
                                />
                            </div>

                            {/* 색상 */}
                            <div>
                                <label className="block text.sm font-medium text-gray-700 mb-1">색상</label>
                                <div className="relative">
                                    <select
                                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 appearance-none cursor-pointer"
                                        value={colorway}
                                        onChange={(e) => setColorway(e.target.value)}
                                    >
                                        <option value="">색상 선택</option>
                                        <option value="black">블랙</option>
                                        <option value="white">화이트</option>
                                        <option value="gray">그레이</option>
                                        <option value="red">레드</option>
                                        <option value="orange">오렌지</option>
                                        <option value="blue">블루</option>
                                        <option value="green">그린</option>
                                        <option value="yellow">옐로우</option>
                                        <option value="pink">핑크</option>
                                        <option value="purple">퍼플</option>
                                        <option value="brown">브라운</option>
                                        <option value="beige">베이지</option>
                                        <option value="navy">네이비</option>
                                        <option value="multi">멀티컬러</option>
                                    </select>
                                    <i className="fas fa-chevron-down absolute right-4 top-1/2 -translate-y-1/2 text-gray-400" />
                                </div>
                            </div>

                            {/* 컨디션 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">컨디션</label>
                                <div className="flex items-center h-[42px] px-3 border border-gray-300 rounded-lg">
                                    <label className="inline-flex items-center mr-6 cursor-pointer">
                                        <input
                                            type="radio"
                                            name="condition"
                                            value="NEW"
                                            checked={condition === "NEW"}
                                            onChange={() => setCondition("NEW")}
                                            className="h-4 w-4 text-blue-600"
                                        />
                                        <span className="ml-2 text-sm text-gray-700">
                      NEW <span className="text-gray-400">/ 새상품</span>
                    </span>
                                    </label>

                                    <label className="inline-flex items-center cursor-pointer">
                                        <input
                                            type="radio"
                                            name="condition"
                                            value="USED"
                                            checked={condition === "USED"}
                                            onChange={() => setCondition("USED")}
                                            className="h-4 w-4 text-blue-600"
                                        />
                                        <span className="ml-2 text-sm text-gray-700">
                      USED <span className="text-gray-400">/ 중고</span>
                    </span>
                                    </label>
                                </div>
                            </div>

                            {/* 출시일 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">출시일(권장)</label>
                                <div className="relative">
                                    <input
                                        type="date"
                                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        min="1980-01-01"
                                        max={new Date().toISOString().split("T")[0]}
                                        value={releaseDate}
                                        onChange={(e) => setReleaseDate(e.target.value)}
                                    />
                                    <i className="fas absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
                                </div>
                            </div>
                        </div>

                        {/* 경매 표시용 */}
                        <div className="grid grid-cols-2 gap-6">
                            {/* 시작가 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">시작가</label>
                                <div className="relative">
                                    <input
                                        type="number"
                                        className="w-full pl-8 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
                                        placeholder="시작가를 입력하세요"
                                        value={startPrice}
                                        onChange={(e) => setStartPrice(e.target.value ? Number(e.target.value) : "")}
                                    />
                                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">₩</span>
                                </div>
                            </div>

                            {/* 경매 종료일 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">경매 종료일(종료 시간은 10분 단위로 조정합니다.)</label>
                                <div className="relative">
                                    <input
                                        type="datetime-local"
                                        className="w-full pl-4 pr-10 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 cursor-pointer"
                                        step={600} // 10분
                                        min={toLocalDatetimeValue(minEndAtDate)}
                                        max={toLocalDatetimeValue(maxEndAtDate)}
                                        value={auctionEndAt}
                                        onChange={(e) => setAuctionEndAt(e.target.value)}
                                        onBlur={(e) => {
                                            if (!e.target.value) return;
                                            let d = roundNearest10Min(new Date(e.target.value));
                                            if (d < minEndAtDate) d = minEndAtDate;
                                            if (d > maxEndAtDate) d = maxEndAtDate;
                                            setAuctionEndAt(toLocalDatetimeValue(d));
                                        }}
                                    />
                                    <i className="fas fa-clock absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
                                </div>
                            </div>
                        </div>

                        {/* 상세 설명 */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">상세 설명</label>
                            <textarea
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                rows={6}
                                placeholder="상품에 대한 상세한 설명을 입력하세요"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                            />
                        </div>

                        {/* 버튼 */}
                        <div className="flex justify-end space-x-4">
                            <button
                                type="button"
                                className="px-6 py-3 border border-gray-300 text-gray-700 !rounded-button hover:bg-gray-50 whitespace-nowrap"
                                onClick={() => window.history.back()}
                            >
                                취소
                            </button>
                            <button
                                type="submit"
                                disabled={submitting}
                                className="px-6 py-3 bg-blue-500 text-white !rounded-button hover:bg-blue-600 disabled:opacity-50 whitespace-nowrap"
                            >
                                {submitting ? "등록 중..." : "등록하기"}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default NewAuctionProductPage;
