import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Toast from "../../../components/Toast";
import { BidForm, BidHeader, BidImageUploader } from "../components/bid";
import BidFormAction from "../components/bid/BidFormAction";
import { brands, categories, sizes } from "../components/bid/mockData";
import type { Bid } from "../types/bid/Bid";


const BidPage = () => {
    const navigate = useNavigate();

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
        const newErrors: { [key: string]: string } = {};
        if (!bidInfo.name.trim()) newErrors.name = "상품명을 입력해주세요.";
        if (!bidInfo.brand) newErrors.brand = "브랜드를 선택해주세요.";
        if (!bidInfo.category) newErrors.category = "카테고리를 선택해주세요.";
        if (!bidInfo.size) newErrors.size = "사이즈를 선택해주세요.";
        if (!bidInfo.startPrice) newErrors.startPrice = "시작가를 입력해주세요.";
        if (!bidInfo.confirmationPrice) newErrors.confirmationPrice = "즉시 구매가를 입력해주세요.";
        if (!bidInfo.startDate) newErrors.startDate = "경매 시작일을 선택해주세요.";
        if (!bidInfo.endDate) newErrors.endDate = "경매 종료일을 선택해주세요.";
        if (!bidInfo.condition) newErrors.condition = "상품 상태를 선택해주세요.";
        if (selectedFiles.length === 0) newErrors.files = "이미지를 1장 이상 등록해주세요.";

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