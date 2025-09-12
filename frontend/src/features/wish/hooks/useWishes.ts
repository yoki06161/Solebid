import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { ApiResponse } from '../../user/types/AuthTypes';
import { addWish, getWishes, removeWish } from '../services/WishService.tsx';
import type { Wish } from '../types/Wish';

const WISHES_QUERY_KEY = 'wishes';

export const useWishes = () => {
    const queryClient = useQueryClient();

    const { data: wishes, isLoading, error } = useQuery<ApiResponse<Wish[]>, Error, Wish[]>({
        queryKey: [WISHES_QUERY_KEY],
        queryFn: getWishes,
        select: (data) => data.data ?? [],
        staleTime: 1000 * 60 * 5,
    });

    const addWishMutation = useMutation({
        mutationFn: (newItem: Wish) => addWish(newItem.id),

        onMutate: async (newItem: Wish) => {
            await queryClient.cancelQueries({ queryKey: [WISHES_QUERY_KEY] });

            const previousResponse = queryClient.getQueryData<ApiResponse<Wish[]>>([WISHES_QUERY_KEY]);

            queryClient.setQueryData<ApiResponse<Wish[]>>([WISHES_QUERY_KEY], (oldResponse) => {
                const oldWishes = oldResponse?.data ?? [];
                return {
                    success: true,
                    data: [...oldWishes, newItem],
                };
            });

            return { previousResponse };
        },

        onError: (_err, _newItem, context) => {
            if (context?.previousResponse) {
                queryClient.setQueryData([WISHES_QUERY_KEY], context.previousResponse);
            }
        },

        onSettled: () => {
            queryClient.invalidateQueries({ queryKey: [WISHES_QUERY_KEY] });
        },
    });

    const removeWishMutation = useMutation({
        mutationFn: removeWish,

        onMutate: async (removedId: number) => {
            await queryClient.cancelQueries({ queryKey: [WISHES_QUERY_KEY] });

            const previousResponse = queryClient.getQueryData<ApiResponse<Wish[]>>([WISHES_QUERY_KEY]);

            queryClient.setQueryData<ApiResponse<Wish[]>>([WISHES_QUERY_KEY], (oldResponse) => {
                if (!oldResponse?.data) {
                    return { success: true, data: [] };
                }
                return {
                    ...oldResponse,
                    data: oldResponse.data.filter((wish) => wish.id !== removedId),
                };
            });

            return { previousResponse };
        },

        onError: (_err, _removedId, context) => {
            if (context?.previousResponse) {
                queryClient.setQueryData([WISHES_QUERY_KEY], context.previousResponse);
            }
        },
        
        onSettled: () => {
            queryClient.invalidateQueries({ queryKey: [WISHES_QUERY_KEY] });
        },
    });

    const isWished = (productId: number) => {
        return wishes?.some(product => product.id === productId) ?? false;
    };

    return {
        wishes,
        isLoading,
        error,
        addWish: addWishMutation.mutate,
        removeWish: removeWishMutation.mutate,
        isWished,
        isAdding: addWishMutation.isPending,
        isRemoving: removeWishMutation.isPending,
    };
};