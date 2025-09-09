import React, { useState } from "react";

type Props = {
    onFilesChange?: (files: File[]) => void;
    max?: number;
};

const ImageGridUploader: React.FC<Props> = ({ onFilesChange, max = 5 }) => {
    const [files, setFiles] = useState<File[]>([]);
    const [previews, setPreviews] = useState<string[]>([]);

    const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
        const add = Array.from(e.target.files ?? []);
        if (add.length + files.length > max) {
            alert(`최대 ${max}장까지만 업로드 가능합니다.`);
            return;
        }
        setFiles((prev) => {
            const next = [...prev, ...add];
            onFilesChange?.(next);
            return next;
        });
        const urls = add.map((f) => URL.createObjectURL(f));
        setPreviews((prev) => [...prev, ...urls]);
    };

    const removeImage = (index: number) => {
        setFiles((prev) => {
            const next = [...prev];
            next.splice(index, 1);
            onFilesChange?.(next);
            return next;
        });
        setPreviews((prev) => {
            const toRevoke = prev[index];
            if (toRevoke) URL.revokeObjectURL(toRevoke);
            const next = [...prev];
            next.splice(index, 1);
            return next;
        });
    };

    return (
        <div>
            <div className="grid grid-cols-5 gap-4">
                {[...Array(max)].map((_, index) => (
                    <div key={index} className="relative aspect-square">
                        {index < previews.length ? (
                            <div className="relative h-full">
                                <img
                                    src={previews[index]}
                                    alt={`Preview ${index + 1}`}
                                    className="w-full h-full object-cover rounded-lg"
                                />
                                <button
                                    type="button"
                                    onClick={() => removeImage(index)}
                                    className="absolute top-2 right-2 w-8 h-8 bg-white rounded-full shadow-md flex items-center justify-center text-gray-600 hover:text-gray-900"
                                >
                                    <i className="fas fa-times"></i>
                                </button>
                            </div>
                        ) : (
                            <label className="h-full flex flex-col items-center justify-center border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-500 cursor-pointer">
                                <i className="fas fa-plus text-gray-400 mb-2"></i>
                                <span className="text-sm text-gray-500">
                  {index === 0 ? "대표 이미지" : "추가 이미지"}
                </span>
                                <input
                                    type="file"
                                    accept="image/*"
                                    className="hidden"
                                    onChange={handleImageUpload}
                                    id={`image-upload-${index}`}
                                />
                            </label>
                        )}
                    </div>
                ))}
            </div>
            <p className="mt-2 text-sm text-gray-500">
                최대 {max}장까지 업로드 가능 (드래그 앤 드롭 지원)
            </p>
        </div>
    );
};

export default ImageGridUploader;
