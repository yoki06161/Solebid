import type { SearchProdudctProps } from "../types/SearchProductProps";

const SearchProduct = ({ products }: SearchProdudctProps) => (
    <div className="px-4 py-6">
        <h3 className="text-lg font-semibold mb-4">
            검색 결과 ({products.length})
        </h3>
        {products.length > 0 ? (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                {products.map((product) => (
                    <div
                        key={product.id}
                        className="bg-white rounded-lg hover:shadow-md transition-shadow cursor-pointer"
                    >
                        <div className="aspect-square overflow-hidden rounded-t-lg">
                            <img
                                src={product.image}
                                alt={product.name}
                                className="w-full h-full object-cover object-top"
                            />
                        </div>
                        <div className="p-3">
                            <p className="text-xs text-gray-500 mb-1">
                                {product.brand}
                            </p>
                            <h4 className="text-sm font-medium text-gray-900 mb-2 line-clamp-2">
                                {product.name}
                            </h4>
                            <p className="text-sm font-bold text-black">
                                {product.price}
                            </p>
                        </div>
                    </div>
                ))}
            </div>
        ) : (
            <div className="text-center py-12">
                <i className="fas fa-search text-gray-300 text-4xl mb-4" />
                <p className="text-gray-500">
                    검색 결과가 없습니다.
                </p>
            </div>
        )}
    </div>
);

export default SearchProduct;