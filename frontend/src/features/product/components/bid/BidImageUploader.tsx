import React from 'react';
import type { BidImageUploaderProps } from '../../types/bid/BidImageUploaderProps';

const BidImageUploader = ({ previewUrls, selectedFiles, onFilesChange }: BidImageUploaderProps) => {
    const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = e.target.files;
        if (files && selectedFiles.length < 5) {
            const newFiles = Array.from(files).slice(0, 5 - selectedFiles.length);
            onFilesChange([...selectedFiles, ...newFiles]);
        }
    };

    const handleRemoveImage = (indexToRemove: number) => {
        onFilesChange(selectedFiles.filter((_, index) => index !== indexToRemove));
    }

    return (
        <div className="mb-8">
            <h2 className="text-lg font-semibold mb-4">
                상품 이미지
            </h2>
            <div className="bg-white border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
                <input
                    type="file"
                    id="imageUpload"
                    multiple
                    accept="image/*"
                    onChange={handleImageUpload}
                    disabled={selectedFiles.length >= 5}
                    className="hidden"
                />
                <label
                    htmlFor="imageUpload"
                    className={selectedFiles.length >= 5 ? "cursor-not-allowed" : "cursor-pointer"}
                >
                    <i className="fas fa-camera text-3xl text-gray-400 mb-4" />
                    <p className="text-gray-600">
                        이미지를 드래그하거나 클릭하여 업로드하세요
                    </p>
                    <p className="text-sm text-gray-400 mt-2">
                        최대 5장까지 등록 가능 ({selectedFiles.length}/5)
                    </p>
                </label>
            </div>
            {previewUrls.length > 0 && (
                <div className="grid grid-cols-5 gap-4 mt-4">
                    {previewUrls.map((image, index) => (
                        <div
                            key={index}
                            className="relative aspect-square bg-gray-100 rounded-lg overflow-hidden"
                        >
                            <img
                                src={image}
                                alt={`미리보기 ${index + 1}`}
                                className="w-full h-full object-cover"
                            />
                            <button
                                onClick={() => handleRemoveImage(index)}
                                className="absolute top-2 right-2 bg-gray-900 bg-opacity-50 text-white rounded-full w-6 h-6 flex items-center justify-center"
                            >
                                <i className="fas fa-times text-sm" />
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default BidImageUploader; 