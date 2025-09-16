import React from "react";

type Props = {
    previews: string[];
    onAdd: (files: File[]) => Promise<unknown> | unknown;
    onRemove: (index: number) => void;
    max?: number;
};

const ImageGridUploader: React.FC<Props> = ({ previews, onAdd, onRemove, max = 5 }) => {
    const handleChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = Array.from(e.target.files || []);
        if (files.length === 0) return;
        await onAdd(files);
        e.currentTarget.value = "";
    };

    return (
        <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">상품 이미지 (최대 {max}장)</label>

            <div className="grid grid-cols-5 gap-4">
                {Array.from({ length: max }).map((_, idx) => (
                    <div key={idx} className="relative aspect-square">
                        {idx < previews.length ? (
                            <div className="relative h-full">
                                <img src={previews[idx]} alt={`preview-${idx}`} className="w-full h-full object-cover rounded-lg" />
                                <button
                                    type="button"
                                    onClick={() => onRemove(idx)}
                                    className="absolute top-2 right-2 w-8 h-8 bg-white rounded-full shadow-md flex items-center justify-center text-gray-600 hover:text-gray-900"
                                    aria-label="remove"
                                >
                                    ✕
                                </button>
                                {idx === 0 && (
                                    <span className="absolute left-2 bottom-2 text-xs px-2 py-1 bg-black/60 text-white rounded">대표</span>
                                )}
                            </div>
                        ) : (
                            <label className="h-full flex flex-col items-center justify-center border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-500 cursor-pointer">
                                <span className="text-gray-400 text-2xl mb-1">＋</span>
                                <span className="text-sm text-gray-500">{idx === 0 ? "대표 이미지" : "추가 이미지"}</span>
                                <input type="file" accept="image/jpeg,image/png,image/jpg" className="hidden" onChange={handleChange} />
                            </label>
                        )}
                    </div>
                ))}
            </div>

            <p className="mt-2 text-sm text-gray-500">JPG/PNG만 업로드 가능</p>
        </div>
    );
};

export default ImageGridUploader;
