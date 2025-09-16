import {useEffect, useState} from 'react';
import type {AuctionItem} from "../features/auction/types/AuctionItem.ts";
import {getPresignedUrls} from '../features/product/services/ProductService.ts';

export const useProductImageUrls = (products: AuctionItem[]) => {
    const [productsWithImages, setProductsWithImages] = useState<AuctionItem[]>([]);
    const [isLoadingImages, setIsLoadingImages] = useState(false);

    const imageKeysString = products
        .map(p => p.image)
        .filter(Boolean)
        .join(',');

    useEffect(() => {
        const fetchImageUrls = async () => {
            if (!products || products.length === 0) {
                setProductsWithImages([]);
                return;
            }

            setIsLoadingImages(true);
            try {
                const imageKeys = imageKeysString
                    ? imageKeysString.split(',')
                    : [];

                if (imageKeys.length === 0) {
                    setProductsWithImages(products);
                    return;
                }

                const presignedUrls = await getPresignedUrls(imageKeys);

                const productsWithUrls = products.map((product) => ({
                    ...product,
                    imageUrl: product.image ? presignedUrls[product.image] || '' : '',
                }));
                setProductsWithImages(productsWithUrls);
            } catch (error) {
                console.error('Error fetching presigned URLs:', error);
                setProductsWithImages(products);
            } finally {
                setIsLoadingImages(false);
            }
        };
        fetchImageUrls().then(r => console.log(r));
    }, [imageKeysString]);

    return {productsWithImages, isLoadingImages};
};
