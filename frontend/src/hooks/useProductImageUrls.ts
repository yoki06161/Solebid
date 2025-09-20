import { useEffect, useState } from 'react';
import type { AuctionItem } from "../features/auction/types/AuctionItem.ts";
import { getPresignedUrls } from '../features/product/services/ProductService.ts';

// 이미지 URL을 가질 수 있는 아이템의 공통 인터페이스
interface ImageableItem {
    image?: string | null;
    productImageUrl?: string | null;
}

// 기본 이미지 키 추출 함수
const defaultImageKeyExtractor = <T extends ImageableItem>(item: T): string | null =>
    item.image || item.productImageUrl || null;

// 이미지 URL 훅
export const useImageUrls = <T extends ImageableItem>(
    items: T[],
    imageKeyExtractor: (item: T) => string | null = defaultImageKeyExtractor
) => {
    const [itemsWithImages, setItemsWithImages] = useState<(T & { imageUrl?: string })[]>([]);
    const [isLoadingImages, setIsLoadingImages] = useState(false);

    const imageKeysString = items
        .map(imageKeyExtractor)
        .filter(Boolean)
        .join(',');

    useEffect(() => {
        const fetchImageUrls = async () => {
            if (!items || items.length === 0) {
                setItemsWithImages([]);
                return;
            }

            setIsLoadingImages(true);
            try {
                const imageKeys = items
                    .map(imageKeyExtractor)
                    .filter((key): key is string => key !== null && key !== undefined);

                if (imageKeys.length === 0) {
                    const itemsWithDefaultImages = items.map(item => ({
                        ...item,
                        imageUrl: '/placeholder-image.jpg'
                    }));
                    setItemsWithImages(itemsWithDefaultImages);
                    return;
                }

                const presignedUrls = await getPresignedUrls(imageKeys);

                const itemsWithUrls = items.map((item) => {
                    const imageKey = imageKeyExtractor(item);
                    return {
                        ...item,
                        imageUrl: imageKey ? presignedUrls[imageKey] || '/placeholder-image.jpg' : '/placeholder-image.jpg',
                    };
                });
                setItemsWithImages(itemsWithUrls);
            } catch (error) {
                console.error('Error fetching presigned URLs:', error);
                const itemsWithDefaultImages = items.map(item => ({
                    ...item,
                    imageUrl: '/placeholder-image.jpg'
                }));
                setItemsWithImages(itemsWithDefaultImages);
            } finally {
                setIsLoadingImages(false);
            }
        };
        fetchImageUrls();
    }, [imageKeysString]); // imageKeyExtractor 제거, imageKeysString만 의존성으로 사용

    return { itemsWithImages, isLoadingImages };
};

// AuctionItem용 이미지 키 추출 함수
const auctionItemImageExtractor = (product: AuctionItem): string | null => product.image;

// 기존 AuctionItem용 훅 (하위 호환성 유지)
export const useProductImageUrls = (products: AuctionItem[]) => {
    const { itemsWithImages, isLoadingImages } = useImageUrls(products, auctionItemImageExtractor);

    return {
        productsWithImages: itemsWithImages,
        isLoadingImages
    };
};
