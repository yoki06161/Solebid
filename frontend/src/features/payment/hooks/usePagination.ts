import { useEffect, useMemo, useState } from 'react';

export function usePagination<T>(
    items: T[],
    options?: { itemsPerPage?: number; initialPage?: number },
) {
    const itemsPerPage = options?.itemsPerPage ?? 10;
    const [currentPage, setCurrentPage] = useState<number>(options?.initialPage ?? 1);

    const totalPages = Math.max(1, Math.ceil(items.length / itemsPerPage));

    const paged = useMemo(() => {
        const start = (currentPage - 1) * itemsPerPage;
        return items.slice(start, start + itemsPerPage);
    }, [items, currentPage, itemsPerPage]);

    // 리스트가 바뀌면 1페이지로 리셋
    useEffect(() => {
        setCurrentPage(1);
    }, [items]);

    const goPrev = () => setCurrentPage((p) => Math.max(1, p - 1));
    const goNext = () => setCurrentPage((p) => Math.min(totalPages, p + 1));

    return { currentPage, setCurrentPage, totalPages, itemsPerPage, paged, goPrev, goNext };
}
