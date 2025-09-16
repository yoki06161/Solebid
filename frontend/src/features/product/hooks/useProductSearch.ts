import { useEffect, useRef, useState } from "react";
import type { Product, Brand, Category } from "../types/product";

type TimeoutId = ReturnType<typeof setTimeout>;

export function useProductSearch(delay = 300) {
    const [term, setTerm] = useState("");
    const [results, setResults] = useState<Product[]>([]);
    const [open, setOpen] = useState(false);
    const timer = useRef<TimeoutId | null>(null); // 초기값 명시

    useEffect(() => {
        if (timer.current) clearTimeout(timer.current);

        if (term.trim().length < 2) {
            setResults([]);
            setOpen(false);
            return;
        }

        timer.current = setTimeout(() => {
            //목업을 서버 enum 값(대문자)으로
            const mock: Product[] = [
                {
                    id: "1",
                    name: "Nike Air Max 270",
                    brand: "NIKE" as Brand,
                    category: "SNEAKERS" as Category,
                    price: 169000,
                    image:
                        "https://readdy.ai/api/search-image?query=professional%20product%20photography%20of%20Nike%20Air%20Max%20270%20sneaker%20on%20clean%20white%20background%20with%20soft%20shadows%20high%20quality%20commercial%20shot&width=100&height=100&seq=1&orientation=squarish",
                },
                {
                    id: "2",
                    name: "Nike Air Force 1",
                    brand: "NIKE" as Brand,
                    category: "SNEAKERS" as Category,
                    price: 139000,
                    image:
                        "https://readdy.ai/api/search-image?query=professional%20product%20photography%20of%20Nike%20Air%20Force%201%20sneaker%20on%20clean%20white%20background%20with%20soft%20shadows%20high%20quality%20commercial%20shot&width=100&height=100&seq=2&orientation=squarish",
                },
                {
                    id: "3",
                    name: "Nike Dunk Low",
                    brand: "NIKE" as Brand,
                    category: "SNEAKERS" as Category,
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

    return { term, setTerm, results, open, setOpen };
}
