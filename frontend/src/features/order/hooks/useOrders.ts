import { useMutation, useQuery, useQueryClient, type UseMutationOptions, type UseQueryOptions } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { getPresignedUrls } from '../../product/services/ProductService.ts';
import { fetchCreateOrder, fetchOrderDetails, fetchWinningOrders, type OrderCreatePayload } from '../services/OrderService.ts';
import type { Order } from '../types/Order.ts';

// Query key factory with better type safety
const orderKeys = {
    all: ['orders'] as const,
    lists: () => [...orderKeys.all, 'list'] as const,
    list: (filters: string) => [...orderKeys.lists(), { filters }] as const,
    details: () => [...orderKeys.all, 'detail'] as const,
    detail: (id: number) => [...orderKeys.details(), id] as const,
};

// Default query options for consistency
const defaultQueryOptions = {
    staleTime: 5 * 60 * 1000,
    retry: 3,
    retryDelay: (attemptIndex: number) => Math.min(1000 * 2 ** attemptIndex, 30000),
} as const;

export const useWinningOrders = (options?: Partial<UseQueryOptions<Order[]>>) => {
    const [ordersWithImages, setOrdersWithImages] = useState<Order[]>([]);
    const [isLoadingImages, setIsLoadingImages] = useState(false);
    const [imageError, setImageError] = useState<string | null>(null);

    const queryResult = useQuery<Order[]>({
        queryKey: orderKeys.list('winnings'),
        queryFn: fetchWinningOrders,
        ...defaultQueryOptions,
        ...options,
    });

    const { data: orders, isLoading, isError } = queryResult;

    useEffect(() => {
        const fetchImageUrls = async () => {
            if (!orders || orders.length === 0) {
                setOrdersWithImages([]);
                return;
            }

            if (isError) {
                setOrdersWithImages(orders);
                return;
            }

            setIsLoadingImages(true);
            setImageError(null);

            try {
                // Extract all image keys from order items
                const imageKeys = orders
                    .flatMap(order => order.items || [])
                    .map(item => item.image)
                    .filter(Boolean);

                if (imageKeys.length === 0) {
                    setOrdersWithImages(orders);
                    return;
                }

                const presignedUrls = await getPresignedUrls(imageKeys);

                const ordersWithUrls = orders.map(order => ({
                    ...order,
                    items: order.items?.map(item => ({
                        ...item,
                        image: item.image ? presignedUrls[item.image] || item.image : item.image,
                    })) || [],
                }));

                setOrdersWithImages(ordersWithUrls);
            } catch (error) {
                console.error('Error fetching presigned URLs for orders:', error);
                setImageError('이미지를 불러오는데 실패했습니다.');
                setOrdersWithImages(orders);
            } finally {
                setIsLoadingImages(false);
            }
        };

        fetchImageUrls();
    }, [orders, isError]);

    return {
        ...queryResult,
        data: ordersWithImages,
        isLoading: isLoading || isLoadingImages,
        imageError,
    };
};

export const useOrderDetails = (
    orderId: number,
    options?: Partial<UseQueryOptions<Order>>
) => {
    const [orderWithImages, setOrderWithImages] = useState<Order | undefined>();
    const [isLoadingImages, setIsLoadingImages] = useState(false);
    const [imageError, setImageError] = useState<string | null>(null);

    const queryResult = useQuery<Order>({
        queryKey: orderKeys.detail(orderId),
        queryFn: () => fetchOrderDetails(orderId),
        enabled: !!orderId,
        ...defaultQueryOptions,
        ...options,
    });

    const { data: order, isLoading, isError } = queryResult;

    useEffect(() => {
        const fetchImageUrls = async () => {
            if (!order) {
                setOrderWithImages(undefined);
                return;
            }

            if (isError) {
                setOrderWithImages(order);
                return;
            }

            setIsLoadingImages(true);
            setImageError(null);

            try {
                const imageKeys = order.items?.map(item => item.image).filter(Boolean) || [];

                if (imageKeys.length === 0) {
                    setOrderWithImages(order);
                    return;
                }

                const presignedUrls = await getPresignedUrls(imageKeys);

                const orderWithUrls = {
                    ...order,
                    items: order.items?.map(item => ({
                        ...item,
                        image: item.image ? presignedUrls[item.image] || item.image : item.image,
                    })) || [],
                };

                setOrderWithImages(orderWithUrls);
            } catch (error) {
                console.error('Error fetching presigned URLs for order:', error);
                setImageError('이미지를 불러오는데 실패했습니다.');
                setOrderWithImages(order);
            } finally {
                setIsLoadingImages(false);
            }
        };

        fetchImageUrls();
    }, [order, isError]);

    return {
        ...queryResult,
        data: orderWithImages,
        isLoading: isLoading || isLoadingImages,
        imageError,
    };
};

export const useCreateOrder = (
    options?: Partial<UseMutationOptions<Order, Error, OrderCreatePayload>>
) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload: OrderCreatePayload) => fetchCreateOrder(payload),
        onSuccess: (data, variables, context) => {
            // Invalidate and refetch related queries
            queryClient.invalidateQueries({ queryKey: orderKeys.lists() });

            // Optionally update cache with new order data if available
            if (data?.id) {
                queryClient.setQueryData(orderKeys.detail(Number(data.id)), data);
            }

            // Call custom onSuccess if provided
            options?.onSuccess?.(data, variables, context);
        },
        onError: (error, variables, context) => {
            console.error('Failed to create order:', error);
            // Call custom onError if provided
            options?.onError?.(error, variables, context);
        },
        ...options,
    });
};
