import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Toast from "../../../components/Toast";
import { BidForm, BidHeader, BidImageUploader } from "../components/bid";
import BidFormAction from "../components/bid/BidFormAction";
import { brands, categories, sizes } from "../components/bid/mockData";
import type { Bid } from "../types/bid/Bid";


const BidPage = () => {
    const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
    const [previewUrls, setPreviewUrls] = useState<string[]>([]);
    const [bidInfo, setBidInfo] = useState<Bid>({
        name: "", brand: "", category: "", size: "", startPrice: "",
        confirmationPrice: "", startDate: "", endDate: "", condition: "", description: "",
    });
    const [errors, setErrors] = useState<{ [key: string]: string }>({});
    const [showSuccessToast, setShowSuccessToast] = useState(false);

    useEffect(() => {
        const newPreviewUrls = selectedFiles.map(file => URL.createObjectURL(file));
        setPreviewUrls(newPreviewUrls);
        return () => {
            newPreviewUrls.forEach(url => URL.revokeObjectURL(url));
        };
    }, [selectedFiles]);

    const navigate = useNavigate();

    const handleBidChange = (field: keyof Bid, value: string) => {
        setBidInfo(prev => ({ ...prev, [field]: value }));
        if (errors[field]) {
            setErrors(prev => {
                const newErrors = { ...prev };
                delete newErrors[field];
                return newErrors;
            });
        }
    };

    const validateForm = () => {
        const validationRules: { key: keyof Bid | 'files'; message: string; isValid: () => boolean }[] = [
            { key: 'name', message: '상품명을 입력해주세요.', isValid: () => !!bidInfo.name.trim() },
            { key: 'brand', message: '브랜드를 선택해주세요.', isValid: () => !!bidInfo.brand },
            { key: 'category', message: '카테고리를 선택해주세요.', isValid: () => !!bidInfo.category },
            { key: 'size', message: '사이즈를 선택해주세요.', isValid: () => !!bidInfo.size },
            { key: 'startPrice', message: '시작가를 입력해주세요.', isValid: () => !!bidInfo.startPrice },
            { key: 'confirmationPrice', message: '즉시 구매가를 입력해주세요.', isValid: () => !!bidInfo.confirmationPrice },
            { key: 'startDate', message: '경매 시작일을 선택해주세요.', isValid: () => !!bidInfo.startDate },
            { key: 'endDate', message: '경매 종료일을 선택해주세요.', isValid: () => !!bidInfo.endDate },
            { key: 'condition', message: '상품 상태를 선택해주세요.', isValid: () => !!bidInfo.condition },
            { key: 'files', message: '이미지를 1장 이상 등록해주세요.', isValid: () => selectedFiles.length > 0 },
        ];

        const newErrors = validationRules.reduce((errors, { key, message, isValid }) => {
            if (!isValid()) {
                errors[key as string] = message;
            }
            return errors;
        }, {} as { [key: string]: string });

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async () => {
        if (!validateForm()) return;

        const formData = new FormData();
        selectedFiles.forEach(file => formData.append("files", file));

        Object.entries(bidInfo).forEach(([key, value]) => {
            formData.append(key, value);
        });

        try {
            const res = await fetch("/api/auction/bid", { method: "POST", body: formData });
            if (!res.ok) {
                const errorData = await res.json();
                throw new Error(errorData.message || "등록에 실패했습니다.");
            }
            setShowSuccessToast(true);
            setTimeout(() => {
                setShowSuccessToast(false);
                navigate("/");
            }, 2000);
        } catch (error) {
            alert(error instanceof Error ? error.message : String(error));
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {
                showSuccessToast && (<Toast message="상품이 성공적으로 등록되었습니다" />)
            }
            <div className="max-w-[1440px] mx-auto">
                <BidHeader
                    title="경매 상품 등록"
                />
                <main className="max-w-3xl mx-auto py-8 px-6">
                    <BidImageUploader
                        previewUrls={previewUrls}
                        selectedFiles={selectedFiles}
                        onFilesChange={setSelectedFiles}
                    />
                    {
                        errors.files &&
                        <p className="text-red-500 text-sm mb-4">
                            {errors.files}
                        </p>
                    }
                    <BidForm
                        bidInfo={bidInfo}
                        errors={errors}
                        onInfoChange={handleBidChange}
                        brands={brands}
                        categories={categories}
                        sizes={sizes}
                    />
                </main>
                <BidFormAction
                    onSubmit={handleSubmit}
                />
            </div>
        </div>
    );
}

export default BidPage;