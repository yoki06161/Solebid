import { useEffect, useRef, useState } from "react";
import type { Product } from "../types/product";

type TimeoutT = ReturnType<typeof setTimeout> | null;

export function useProductSearch(delay = 300) {
    const [term, setTerm] = useState("");
    const [results, setResults] = useState<Product[]>([]);
    const [open, setOpen] = useState(false);
    const timer = useRef<TimeoutT>(null);

    useEffect(() => {
        if (timer.current) clearTimeout(timer.current);
        if (term.trim().length < 2) {
            setResults([]);
            setOpen(false);
            return;
        }

        timer.current = setTimeout(() => {
            // TODO: 실제 API로 교체
            const mock: Product[] = [
                {
                    id: "1",
                    name: "Nike Air Max 270",
                    brand: "nike",
                    category: "sneakers",
                    price: 169000,
                    image:
                        "https://readdy.ai/api/search-image?query=professional%20product%20photography%20of%20Nike%20Air%20Max%20270%20sneaker%20on%20clean%20white%20background%20with%20soft%20shadows%20high%20quality%20commercial%20shot&width=100&height=100&seq=1&orientation=squarish",
                },
                {
                    id: "2",
                    name: "Nike Air Force 1",
                    brand: "nike",
                    category: "sneakers",
                    price: 139000,
                    image:
                        "https://readdy.ai/api/search-image?query=professional%20product%20photography%20of%20Nike%20Air%20Force%201%20sneaker%20on%20clean%20white%20background%20with%20soft%20shadows%20high%20quality%20commercial%20shot&width=100&height=100&seq=2&orientation=squarish",
                },
                {
                    id: "3",
                    name: "Nike Dunk Low",
                    brand: "nike",
                    category: "sneakers",
                    price: 149000,
                    image:
                        "https://readdy.ai/api/search-image?query=professional%20product%20photography%20of%20Nike%20Dunk%20Low%20sneaker%20on%20clean%20white%20background%20with%20soft%20shadows%20high%20quality%20commercial%20shot&width=100&height=100&seq=3&orientation=squarish",
                },
            ].filter((p) => p.name.toLowerCase().includes(term.toLowerCase()));

            setResults(mock);
            setOpen(mock.length > 0);
        }, delay);

        return () => {
            if (timer.current) clearTimeout(timer.current);
        };
    }, [term, delay]);

    return {
        term,
        setTerm,
        results,
        open,
        setOpen,
    };
}
